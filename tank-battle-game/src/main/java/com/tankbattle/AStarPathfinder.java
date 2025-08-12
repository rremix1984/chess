package com.tankbattle;

import java.util.*;

public class AStarPathfinder {

    private static class Node implements Comparable<Node> {
        int x, y;
        double gCost, hCost, fCost;
        Node parent;

        Node(int x, int y, double gCost, double hCost, Node parent) {
            this.x = x;
            this.y = y;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
            this.parent = parent;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fCost, other.fCost);
        }
    }

    private int gridWidth;
    private int gridHeight;
    private int[][] grid;
    private List<Obstacle> obstacles;

    public AStarPathfinder(int gridWidth, int gridHeight, List<Obstacle> obstacles) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.obstacles = obstacles;
        this.grid = new int[gridWidth][gridHeight];
        defineGrid();
    }

    private void defineGrid() {
        for (Obstacle obstacle : obstacles) {
            for (int i = obstacle.getX(); i < obstacle.getX() + obstacle.getWidth(); i++) {
                for (int j = obstacle.getY(); j < obstacle.getY() + obstacle.getHeight(); j++) {
                    if (i >= 0 && i < gridWidth && j >= 0 && j < gridHeight) {
                        grid[i][j] = 1; // Mark as occupied
                    }
                }
            }
        }
    }

    public List<int[]> findPath(int startX, int startY, int endX, int endY) {
        PriorityQueue<Node> openList = new PriorityQueue<>();
        boolean[][] closedList = new boolean[gridWidth][gridHeight];

        Node startNode = new Node(startX, startY, 0, heuristic(startX, startY, endX, endY), null);
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();

            if (currentNode.x == endX && currentNode.y == endY) {
                return reconstructPath(currentNode);
            }

            closedList[currentNode.x][currentNode.y] = true;

            for (int[] direction : new int[][]{{0, 1}, {1, 0}, {0, -1}, {-1, 0}}) {
                int newX = currentNode.x + direction[0];
                int newY = currentNode.y + direction[1];

                if (isValidMove(newX, newY, closedList)) {
                    double gCost = currentNode.gCost + 1;
                    double hCost = heuristic(newX, newY, endX, endY);
                    Node neighbor = new Node(newX, newY, gCost, hCost, currentNode);

                    if (openList.contains(neighbor) && gCost >= neighbor.gCost) continue;

                    openList.add(neighbor);
                }
            }
        }
        return null; // No path found
    }

    private boolean isValidMove(int x, int y, boolean[][] closedList) {
        return x >= 0 && x < gridWidth && y >= 0 && y < gridHeight && grid[x][y] == 0 && !closedList[x][y];
    }

    private double heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private List<int[]> reconstructPath(Node node) {
        List<int[]> path = new LinkedList<>();
        while (node != null) {
            path.add(0, new int[]{node.x, node.y});
            node = node.parent;
        }
        return path;
    }
}
