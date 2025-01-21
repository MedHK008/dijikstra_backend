package com.dijikstra.dijikstraV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maze")
@CrossOrigin(origins = "https://dijikstravisualisation.netlify.app/")
public class MazeController {

    @PostMapping("/dijkstra")
    public Map<String, List<int[]>> findShortestPath(@RequestBody MazeRequest request) {
        int[][] maze = request.getMaze();
        int[] start = request.getStart();
        int[] end = request.getEnd();

        Map<String, List<int[]>> result = new HashMap<>();
        List<int[]> visitedNodes = new ArrayList<>();
        List<int[]> path = dijkstra(maze, start, end, visitedNodes);

        result.put("visitedNodes", visitedNodes);
        result.put("path", path);
        return result;
    }

    @PostMapping("/astar")
    public Map<String, List<int[]>> findShortestPathAStar(@RequestBody MazeRequest request) {
        int[][] maze = request.getMaze();
        int[] start = request.getStart();
        int[] end = request.getEnd();

        Map<String, List<int[]>> result = new HashMap<>();
        List<int[]> visitedNodes = new ArrayList<>();
        List<int[]> path = aStar(maze, start, end, visitedNodes);

        result.put("visitedNodes", visitedNodes);
        result.put("path", path);
        return result;
    }

    private List<int[]> dijkstra(int[][] maze, int[] start, int[] end, List<int[]> visitedNodes) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        pq.add(new Node(start[0], start[1], 0));

        int rows = maze.length, cols = maze[0].length;
        int[][] distances = new int[rows][cols];
        for (int[] row : distances) Arrays.fill(row, Integer.MAX_VALUE);

        distances[start[0]][start[1]] = 0;
        boolean[][] visited = new boolean[rows][cols];

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        List<int[]> path = new ArrayList<>();

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (visited[current.x][current.y]) continue;
            visited[current.x][current.y] = true;

            // Add the current cell to visited nodes for visualization
            visitedNodes.add(new int[]{current.x, current.y});

            if (current.x == end[0] && current.y == end[1]) {
                return reconstructPath(current);
            }

            for (int[] dir : directions) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];

                if (nx >= 0 && ny >= 0 && nx < rows && ny < cols && maze[nx][ny] == 0) {
                    int newDist = current.distance + 1;
                    if (newDist < distances[nx][ny]) {
                        distances[nx][ny] = newDist;
                        pq.add(new Node(nx, ny, newDist, current));
                    }
                }
            }
        }

        return path; // No path found
    }

    private List<int[]> aStar(int[][] maze, int[] start, int[] end, List<int[]> visitedNodes) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        pq.add(new Node(start[0], start[1], 0, heuristic(start, end)));

        int rows = maze.length, cols = maze[0].length;
        int[][] gScores = new int[rows][cols];
        for (int[] row : gScores) Arrays.fill(row, Integer.MAX_VALUE);

        gScores[start[0]][start[1]] = 0;
        boolean[][] visited = new boolean[rows][cols];

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        List<int[]> path = new ArrayList<>();

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (visited[current.x][current.y]) continue;
            visited[current.x][current.y] = true;

            // Add the current cell to visited nodes for visualization
            visitedNodes.add(new int[]{current.x, current.y});

            if (current.x == end[0] && current.y == end[1]) {
                return reconstructPath(current);
            }

            for (int[] dir : directions) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];

                if (nx >= 0 && ny >= 0 && nx < rows && ny < cols && maze[nx][ny] == 0) {
                    int tentativeGScore = gScores[current.x][current.y] + 1;
                    if (tentativeGScore < gScores[nx][ny]) {
                        gScores[nx][ny] = tentativeGScore;
                        int fScore = tentativeGScore + heuristic(new int[]{nx, ny}, end);
                        pq.add(new Node(nx, ny, tentativeGScore, fScore, current));
                    }
                }
            }
        }

        return path; // No path found
    }

    private int heuristic(int[] a, int[] b) {
        return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]);
    }

    private List<int[]> reconstructPath(Node endNode) {
        List<int[]> path = new ArrayList<>();
        Node current = endNode;
        while (current != null) {
            path.add(new int[]{current.x, current.y});
            current = current.previous;
        }
        Collections.reverse(path);
        return path;
    }

    static class Node {
        int x, y, distance;
        int g, f;
        Node previous;

        Node(int x, int y, int distance) {
            this(x, y, distance, null);
        }

        Node(int x, int y, int distance, Node previous) {
            this.x = x;
            this.y = y;
            this.distance = distance;
            this.previous = previous;
        }

        Node(int x, int y, int g, int f) {
            this(x, y, g, f, null);
        }

        Node(int x, int y, int g, int f, Node previous) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
            this.previous = previous;
        }
    }

    static class MazeRequest {
        private int[][] maze;
        private int[] start;
        private int[] end;

        // Getters and setters
        public int[][] getMaze() { return maze; }
        public void setMaze(int[][] maze) { this.maze = maze; }
        public int[] getStart() { return start; }
        public void setStart(int[] start) { this.start = start; }
        public int[] getEnd() { return end; }
        public void setEnd(int[] end) { this.end = end; }
    }
}
