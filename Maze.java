package HW.hw10;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.*;

//To play the maze game, press "m"
//Then use the four arrow keys to navigate
//To use Breadth Search, press "b"
//To use Depth Search, press "d"

// to represent a cell in the maze
class Vertex {
    int x;
    int y;
    ArrayList<Edge> outEdges;
    Vertex left;
    Vertex right;
    Vertex up;
    Vertex down;
    boolean visited;
    boolean current;

    // constructor for a node
    Vertex(int x, int y) {
        this.outEdges = new ArrayList<Edge>();
        this.x = x;
        this.y = y;
        this.left = null;
        this.right = null;
        this.up = null;
        this.down = null;
        this.visited = false;
        this.current = false;
    }

    // draws a vertex
    public WorldImage drawVertex(int scale, Vertex start, Vertex end) {
        Color color;
        if (current) {
            color = Color.gray;
        }
        else if (visited) {
            color = Color.LIGHT_GRAY;
        }
        else if (this.equals(start)) {
            color = Color.PINK;
        }
        else if (this.equals(end)) {
            color = Color.GREEN;
        }
        else {
            color = Color.white;
        }
        return new RectangleImage(scale - 2, scale - 2, OutlineMode.SOLID, color);
    }

    // Switches the visited from its initial state to the opposite boolean
    // EFFECT: mutates the visted field to be the opposite of its original
    public void highlight() {
        this.visited = true;
    }

    // Switches the current from its initial state to the opposite boolean
    // EFFECT: mutates the current field to be the opposite of its original
    public void onCurrent() {
        this.current = !this.current;
    }

    // adds to the lists of all the visited cells
    public void addNeighbors(ICollection<Vertex> worklist, ArrayList<Edge> edgesInSpan) {
        this.visited = true;
        if (edgesInSpan.contains(new Edge(this, this.down, 0))) {
            worklist.add(this.down);
        }
        if (edgesInSpan.contains(new Edge(this, this.left, 0))) {
            worklist.add(this.left);
        }
        if (edgesInSpan.contains(new Edge(this, this.up, 0))) {
            worklist.add(this.up);
        }
        if (edgesInSpan.contains(new Edge(this, this.right, 0))) {
            worklist.add(this.right);
        }
    }
}

// to represent an edge, made of a from and to node
class Edge implements Comparable<Edge> {
    Vertex from;
    Vertex to;
    int weight;

    // constructor for an edge with an assigned weight
    Edge(Vertex from, Vertex to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    // draws all the edges on the board
    WorldImage drawHorizontalEdge(int scale) {
        return new RectangleImage(scale, 1, OutlineMode.SOLID, Color.BLACK);
    }

    // draws all the vertical lines on the board
    WorldImage drawVerticalEdge(int scale) {
        return new RectangleImage(1, scale, OutlineMode.SOLID, Color.BLACK);
    }

    // compares the weight of two edges
    public int compareTo(Edge o) {
        return Integer.compare(this.weight, o.weight);
    }

    // determines if the object isn't a type of Edge
    public boolean equals(Object o) {
        if (!(o instanceof Edge)) {
            return false;
        }
        else {
            Edge cur = (Edge) o;
            return (cur.from == this.from && cur.to == this.to)
                    || (cur.to == this.from && cur.from == this.to);
        }
    }
}

// to represent a maze
class Maze extends World {
    ArrayList<ArrayList<Vertex>> vertices;
    // height, width, and size of vertex
    public int height;
    public int width;
    // all vertices in the board
    ArrayList<Edge> edgesInTree;
    // all edges in graph, sorted by edge weight
    ArrayList<Edge> worklist;
    HashMap<Vertex, Vertex> representatives;
    Random rand;
    int scale;
    int size;
    Vertex cellOn;
    boolean manual;
    boolean bfs;
    boolean dfs;
    Vertex topLeft;
    Vertex bottomRight;
    SearchStepper bfsCur;
    SearchStepper dfsCur;

    // constructor for Maze
    Maze(int width, int height) {

        this.width = width;
        this.height = height;
        this.size = 500;
        this.scale = this.size / Math.max(width, height);
        this.vertices = this.createVertices();
        this.rand = new Random();
        this.createEdges();
        this.representatives = new HashMap<Vertex, Vertex>();
        this.edgesInTree = new ArrayList<Edge>();
        this.worklist = new ArrayList<Edge>();
        this.createRepresentatives();
        this.initializeWorklist();
        this.kruskals();
        bfsCur = new SearchStepper(topLeft, bottomRight, new Queue<Vertex>());
        dfsCur = new SearchStepper(topLeft, bottomRight, new Stack<Vertex>());
    }

    // constructor for Maze, used for testing with a given random seed
    Maze(int width, int height, Random rand) {

        this.width = width;
        this.height = height;
        this.size = 500;
        this.scale = this.size / Math.max(width, height);
        this.vertices = this.createVertices();
        this.rand = rand;
        this.createEdges();
        this.representatives = new HashMap<Vertex, Vertex>();
        this.edgesInTree = new ArrayList<Edge>();
        this.worklist = new ArrayList<Edge>();
        bfsCur = new SearchStepper(topLeft, bottomRight, new Queue<Vertex>());
        dfsCur = new SearchStepper(topLeft, bottomRight, new Stack<Vertex>());

    }

    // creates a list of vertices for the board
    public ArrayList<ArrayList<Vertex>> createVertices() {
        ArrayList<ArrayList<Vertex>> vertices = new ArrayList<ArrayList<Vertex>>();
        for (int i = 0; i < this.height; i++) {
            ArrayList<Vertex> row = new ArrayList<Vertex>();

            for (int j = 0; j < this.width; j++) {
                Vertex newV = new Vertex(j, i);
                if (i == 0 && j == 0) {
                    topLeft = newV;
                }
                else if (i == this.height - 1 && j == this.width - 1) {
                    bottomRight = newV;
                }
                row.add(newV);
            }

            vertices.add(row);
        }

        Vertex current;
        for (int a = 0; a < this.height; a++) {
            for (int b = 0; b < this.width; b++) {
                current = vertices.get(a).get(b);
                if (a == 0) {
                    current.up = null;
                }
                else {
                    current.up = vertices.get(a - 1).get(b);
                }
                if (b == 0) {
                    current.left = null;
                }
                else {
                    current.left = vertices.get(a).get(b - 1);
                }
                if (a == this.height - 1) {
                    current.down = null;
                }
                else {
                    current.down = vertices.get(a + 1).get(b);
                }
                if (b == this.width - 1) {
                    current.right = null;
                }
                else {
                    current.right = vertices.get(a).get(b + 1);
                }
                vertices.get(a).set(b, current);
            }
        }

        return vertices;
    }

    // creates a list of edges for the board
    public void createEdges() {
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                Vertex v = this.vertices.get(i).get(j);

                if (j < this.width - 1) {
                    v.outEdges.add(new Edge(v, this.vertices.get(i).get(j + 1), this.rand.nextInt()));
                }

                if (i < this.height - 1) {
                    v.outEdges.add(new Edge(v, this.vertices.get(i + 1).get(j), this.rand.nextInt()));
                }
            }
        }
    }

    // creates an initial hashmap and initializes every node's representative
    // to itself
    public void createRepresentatives() {
        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                Vertex vertex = this.vertices.get(row).get(col);
                this.representatives.put(vertex, vertex);
            }
        }
    }

    // creates an initial worklist, consisting of the edges of the vertices
    // and sorts them by weight
    public void initializeWorklist() {
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                Vertex v = this.vertices.get(i).get(j);
                for (Edge edge : v.outEdges) {
                    this.worklist.add(edge);
                }
            }
        }

        Collections.sort(this.worklist);
    }

    //finds the key for the hashmap
    public Vertex find(Vertex key) {
        while (!this.representatives.get(key).equals(key)) {
            key = this.representatives.get(key);
        }

        return key;
    }

    // maps to and from in the representatives
    public void union(Vertex to, Vertex from) {
        this.representatives.put(to, from);
    }

    //
    public boolean edgeInTree(Vertex from, Vertex to) {
        for (Edge edge : this.edgesInTree) {
            if ((edge.from.equals(from) && edge.to.equals(to))
                    || (edge.from.equals(to) && edge.to.equals(from))) {
                return true;
            }
        }
        return false;
    }

    // implementation of kruskals algo
    public void kruskals() {
        while (this.edgesInTree.size() < this.height * this.width - 1) {

            Edge next = worklist.remove(0);
            Vertex from = find(next.from);
            Vertex to = find(next.to);

            if (!from.equals(to)) {
                edgesInTree.add(next);
                union(from, to);
            }
        }
    }

    // actions for the movement of the arrow keys
    void move(String key) {
        if (manual) {
            if (key.equals("up")) {
                if (this.edgesInTree.contains(new Edge(cellOn, cellOn.up, 0))) {
                    cellOn.highlight();
                    cellOn.onCurrent();
                    cellOn.up.onCurrent();
                    cellOn = this.cellOn.up;
                }
            }
            else if (key.equals("down")) {
                if (this.edgesInTree.contains(new Edge(cellOn, cellOn.down, 0))) {
                    cellOn.highlight();
                    cellOn.onCurrent();
                    cellOn.down.onCurrent();
                    cellOn = this.cellOn.down;
                }
            }
            else if (key.equals("right")) {
                if (this.edgesInTree.contains(new Edge(cellOn, cellOn.right, 0))) {
                    cellOn.highlight();
                    cellOn.onCurrent();
                    cellOn.right.onCurrent();
                    cellOn = this.cellOn.right;
                }
            }
            else if (key.equals("left")) {
                if (this.edgesInTree.contains(new Edge(cellOn, cellOn.left, 0))) {
                    cellOn.highlight();
                    cellOn.onCurrent();
                    cellOn.left.onCurrent();
                    cellOn = this.cellOn.left;
                }
            }
        }
    }

    // initiates breadth search
    void bfs(Vertex from, Vertex to) {
        new SearchStepper(from, to, new Queue<Vertex>());
    }

    // initiates depth search
    void dfs(Vertex from, Vertex to) {
        new SearchStepper(from, to, new Stack<Vertex>());
    }

    // Draws the maze
    public WorldScene makeScene() {
        WorldScene scene = new WorldScene(this.size, this.size);

        Vertex start = this.vertices.get(0).get(0);
        Vertex end = this.vertices.get(height - 1).get(width - 1);

        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                Vertex vertex = this.vertices.get(row).get(col);
                int x = vertex.x * this.scale;
                int y = vertex.y * this.scale;

                scene.placeImageXY(vertex.drawVertex(this.scale, start, end),
                        x + this.scale / 2, y + this.scale / 2);

                if (col < this.width - 1 && !edgeInTree(vertex, this.vertices.get(row).get(col + 1))) {
                    WorldImage line = new Edge(vertex, vertex, 0).drawVerticalEdge(this.scale);
                    scene.placeImageXY(line, x + this.scale, y + this.scale / 2);
                }

                if (row < this.height - 1 && !edgeInTree(vertex, this.vertices.get(row + 1).get(col))) {
                    WorldImage line = new Edge(vertex, vertex, 0).drawHorizontalEdge(this.scale);
                    scene.placeImageXY(line, x + this.scale / 2, y + this.scale);
                }

                if (row == this.height - 1) {
                    WorldImage line = new Edge(vertex, vertex, 0).drawHorizontalEdge(this.scale);
                    scene.placeImageXY(line, x + this.scale / 2, y + this.scale);
                }
                if (col == this.width - 1) {
                    WorldImage line = new Edge(vertex, vertex, 0).drawVerticalEdge(this.scale);
                    scene.placeImageXY(line, x + this.scale, y + this.scale / 2);
                }
            }
        }
        return scene;
    }

    // takes in all the key actions
    public void onKeyEvent(String key) {
        if (key.equals("m")) {
            this.cellOn = this.topLeft;
            topLeft.onCurrent();
            manual = true;
        }
        else if (key.equals("b")) {
            this.bfs = true;
        }
        else if (key.equals("d")) {
            this.dfs = true;
        }
        else if (key.equals("r")) {
            this.size = 500;
            this.scale = this.size / Math.max(width, height);
            this.vertices = this.createVertices();
            this.rand = new Random();
            this.createEdges();
            this.representatives = new HashMap<Vertex, Vertex>();
            this.edgesInTree = new ArrayList<Edge>();
            this.worklist = new ArrayList<Edge>();
            this.createRepresentatives();
            this.initializeWorklist();
            this.kruskals();
            bfsCur = new SearchStepper(topLeft, bottomRight, new Queue<Vertex>());
            dfsCur = new SearchStepper(topLeft, bottomRight, new Stack<Vertex>());
            new Maze(this.width, this.height);
            this.bfs = false;
            this.dfs = false;
        }
        this.move(key);
    }

    // takes in all the key actions, to test with RANDOM
    public void onKeyEventTest(String key, Random rand) {
        if (key.equals("m")) {
            this.cellOn = this.topLeft;
            topLeft.onCurrent();
            manual = true;
        }
        else if (key.equals("b")) {
            this.bfs = true;
        }
        else if (key.equals("d")) {
            this.dfs = true;
        }
        else if (key.equals("r")) {
            this.size = 500;
            this.scale = this.size / Math.max(width, height);
            this.vertices = this.createVertices();
            this.rand = rand;
            this.createEdges();
            this.representatives = new HashMap<Vertex, Vertex>();
            this.edgesInTree = new ArrayList<Edge>();
            this.worklist = new ArrayList<Edge>();
            this.createRepresentatives();
            this.initializeWorklist();
            this.kruskals();
            bfsCur = new SearchStepper(topLeft, bottomRight, new Queue<Vertex>());
            dfsCur = new SearchStepper(topLeft, bottomRight, new Stack<Vertex>());
            new Maze(this.width, this.height);
            this.bfs = false;
            this.dfs = false;
        }
        this.move(key);
    }

    // goes over every second for breadth and depth search
    public void onTick() {
        if (bfs) {
            bfsCur.step(this.edgesInTree);
        }
        else if (dfs) {
            dfsCur.step(this.edgesInTree);
        }
    }
}

// the class for the search algorithms
class SearchStepper {
    Deque<Vertex> alreadySeen;
    Vertex to;
    ICollection<Vertex> workList;
    boolean done;

    SearchStepper(Vertex from, Vertex to, ICollection<Vertex> worklist) {
        this.alreadySeen = new ArrayDeque<Vertex>();
        this.to = to;
        this.workList = worklist;
        worklist.add(from);
        done = false;
    }

    // goes over all the visited cells and highlights them
    public void step(ArrayList<Edge> existingSpan) {
        if (!(this.workList.isEmpty() || this.done)) {
            Vertex next = this.workList.remove();
            if (next.equals(to)) {
                next.highlight();
                this.done = true;
            }
            else if (alreadySeen.contains(next)) {
                // Do nothing
            }
            else {
                next.addNeighbors(this.workList, existingSpan);
            }
            this.alreadySeen.add(next);
        }
    }
}

//Represents a mutable collection of items
interface ICollection<T> {
    // Is this collection empty?
    boolean isEmpty();

    // EFFECT: adds the item to the collection
    void add(T item);

    // Returns the first item of the collection
    // EFFECT: removes that first item
    T remove();

    // returns the size of the collection
    public int size();
}

// to initiate LIFO behavior
class Stack<T> implements ICollection<T> {
    Deque<T> contents;

    Stack() {
        this.contents = new ArrayDeque<T>();
    }

    // is this stack empty?
    public boolean isEmpty() {
        return this.contents.isEmpty();
    }

    // Returns the first item of this stack
    // EFFECT: removes that first item
    public T remove() {
        return this.contents.removeFirst();
    }

    //EFFECT: adds the item to the stack
    public void add(T item) {
        this.contents.addFirst(item);
    }

    // returns the size of the stack
    public int size() {
        return this.contents.size();
    }
}

// to initiate FIFO behavior
class Queue<T> implements ICollection<T> {
    Deque<T> contents;

    Queue() {
        this.contents = new ArrayDeque<T>();
    }

    // is the queue empty?
    public boolean isEmpty() {
        return this.contents.isEmpty();
    }

    // Returns the first item of this queue
    // EFFECT: removes that first item
    public T remove() {
        return this.contents.removeFirst();
    }

    //EFFECT: adds the item to the queue
    public void add(T item) {
        this.contents.addLast(item); // NOTE: Different from Stack!
    }

    //returns the size of the stack
    public int size() {
        return this.contents.size();
    }
}

// examples class for the maze
class ExamplesMaze {

    Vertex vert1 = new Vertex(0, 0);
    Vertex vert2 = new Vertex(0, 1);
    Vertex vert3 = new Vertex(1, 0);
    Vertex vert4 = new Vertex(1, 1);
    Vertex vert5 = new Vertex(1, 2);
    Vertex vert6 = new Vertex(2, 0);
    Vertex vert7 = new Vertex(1, 1);

    Edge edge1 = new Edge(vert1, vert3, 0);
    Edge edge2 = new Edge(vert1, vert2, 0);
    Edge edge3 = new Edge(vert4, vert5, 1);
    Edge edge4 = new Edge(vert4, vert7, 1);

    ICollection<Integer> stack1;
    ICollection<String> stack2;

    ICollection<Integer> queue1;
    ICollection<String> queue2;

    Maze maze = new Maze(40, 40);
    Maze maze2 = new Maze(10, 10);
    Maze mazeTest = new Maze(4, 4, new Random(4));
    Maze mazeTest2 = new Maze(5, 5, new Random(4));

    void testBigBang(Tester t) {
        maze.bigBang(500, 500, 0.00000000000000000000001);
    }

    //examples of stack and queues
    void initCollection() {

        stack1 = new Stack<Integer>();
        stack2 = new Stack<String>();

        queue1 = new Queue<Integer>();
        queue2 = new Queue<String>();
    }

    // tests for the method isEmpty
    void testIsEmpty(Tester t) {
        initCollection();

        t.checkExpect(stack1.isEmpty(), true);
        t.checkExpect(stack2.isEmpty(), true);
        t.checkExpect(queue1.isEmpty(), true);
        t.checkExpect(queue2.isEmpty(), true);

        // changing conditions
        stack1.add(1);
        queue2.add("hi");

        t.checkExpect(stack1.isEmpty(), false);
        t.checkExpect(stack2.isEmpty(), true);
        t.checkExpect(queue1.isEmpty(), true);
        t.checkExpect(queue2.isEmpty(), false);
    }

    // tests for the size method
    void testSize(Tester t) {
        initCollection();

        t.checkExpect(stack1.isEmpty(), true);
        t.checkExpect(stack2.isEmpty(), true);
        t.checkExpect(queue1.isEmpty(), true);
        t.checkExpect(queue2.isEmpty(), true);

        // changing conditions
        stack1.add(1);
        t.checkExpect(stack1.size(), 1);
        stack1.add(2);
        stack1.add(3);
        t.checkExpect(stack1.size(), 3);
        stack1.add(5);
        t.checkExpect(stack1.size(), 4);

        t.checkExpect(stack1.remove(), 5);
        t.checkExpect(stack1.size(), 3);

        queue2.add("hi");
        t.checkExpect(queue2.size(), 1);
        queue2.add("ice cream");
        queue2.add("dogs");
        t.checkExpect(queue2.size(), 3);

        t.checkExpect(queue2.remove(), "hi");
        t.checkExpect(queue2.remove(), "ice cream");
        t.checkExpect(queue2.size(), 1);
    }

    // tests for the remove method
    void testRemove(Tester t) {
        initCollection();

        t.checkExpect(stack1.isEmpty(), true);
        t.checkExpect(stack2.isEmpty(), true);
        t.checkExpect(queue1.isEmpty(), true);
        t.checkExpect(queue2.isEmpty(), true);

        // changing conditions
        stack1.add(1);
        stack1.add(2);
        stack1.add(3);
        t.checkExpect(stack1.size(), 3);

        t.checkExpect(stack1.remove(), 3);
        t.checkExpect(stack1.remove(), 2);
        t.checkExpect(stack1.remove(), 1);
        t.checkExpect(stack1.size(), 0);

        queue2.add("hi");
        queue2.add("ice cream");
        queue2.add("dogs");
        t.checkExpect(queue2.size(), 3);

        t.checkExpect(queue2.remove(), "hi");
        t.checkExpect(queue2.remove(), "ice cream");
        t.checkExpect(queue2.remove(), "dogs");
        t.checkExpect(queue2.size(), 0);
    }

    // tests for the method add
    void testAdd(Tester t) {
        initCollection();

        t.checkExpect(stack1.isEmpty(), true);
        t.checkExpect(stack2.isEmpty(), true);
        t.checkExpect(queue1.isEmpty(), true);
        t.checkExpect(queue2.isEmpty(), true);

        // changing conditions
        stack1.add(1);
        t.checkExpect(stack1.size(), 1);
        stack1.add(2);
        t.checkExpect(stack1.size(), 2);
        stack1.add(3);
        t.checkExpect(stack1.size(), 3);
        t.checkExpect(stack1.remove(), 3);

        stack2.add("hi");
        t.checkExpect(stack2.size(), 1);
        stack2.add("flowers");
        t.checkExpect(stack2.size(), 2);
        stack2.add("bees");
        t.checkExpect(stack2.size(), 3);
        t.checkExpect(stack2.remove(), "bees");

        queue1.add(1);
        t.checkExpect(queue1.size(), 1);
        queue1.add(2);
        t.checkExpect(queue1.size(), 2);
        queue1.add(3);
        t.checkExpect(queue1.size(), 3);
        t.checkExpect(queue1.remove(), 1);
    }


    // tests the drawVertex method
    void testDrawVertex(Tester t) {
        Maze test = new Maze(4, 4, new Random(4));
        Vertex start = test.vertices.get(0).get(0);
        Vertex end = test.vertices.get(test.height - 1).get(test.width - 1);

        t.checkExpect(test.vertices.get(0).get(0).drawVertex(10, start, end),
                new RectangleImage(8, 8, OutlineMode.SOLID, Color.pink));
        t.checkExpect(test.vertices.get(1).get(1).drawVertex(5, start, end),
                new RectangleImage(3, 3, OutlineMode.SOLID, Color.white));
        t.checkExpect(test.vertices.get(1).get(2).drawVertex(2, start, end),
                new RectangleImage(0, 0, OutlineMode.SOLID, Color.white));
        t.checkExpect(test.vertices.get(3).get(3).drawVertex(2, start, end),
                new RectangleImage(0, 0, OutlineMode.SOLID, Color.green));

        Maze test2 = new Maze(6, 6, new Random(4));
        Vertex start2 = test2.vertices.get(0).get(0);
        Vertex end2 = test2.vertices.get(test2.height - 1).get(test2.width - 1);

        t.checkExpect(test2.vertices.get(0).get(0).drawVertex(10, start2, end2),
                new RectangleImage(8, 8, OutlineMode.SOLID, Color.pink));
        t.checkExpect(test2.vertices.get(1).get(1).drawVertex(20, start2, end2),
                new RectangleImage(18, 18, OutlineMode.SOLID, Color.white));
        t.checkExpect(test2.vertices.get(1).get(2).drawVertex(25, start2, end2),
                new RectangleImage(23, 23, OutlineMode.SOLID, Color.white));
        t.checkExpect(test2.vertices.get(5).get(5).drawVertex(25, start2, end2),
                new RectangleImage(23, 23, OutlineMode.SOLID, Color.green));
    }


    // tests the drawHorizontalEdge method
    void testDrawHorizontalEdge(Tester t) {
        Maze test = new Maze(4, 4, new Random(4));
        t.checkExpect(test.vertices.get(0).get(0).outEdges.get(0).drawHorizontalEdge(10),
                new RectangleImage(10, 1, OutlineMode.SOLID, Color.black));
        t.checkExpect(test.vertices.get(0).get(0).outEdges.get(1).drawHorizontalEdge(100),
                new RectangleImage(100, 1, OutlineMode.SOLID, Color.black));
        t.checkExpect(test.vertices.get(0).get(1).outEdges.get(1).drawHorizontalEdge(100),
                new RectangleImage(100, 1, OutlineMode.SOLID, Color.black));

        Maze test2 = new Maze(6, 6, new Random(4));
        t.checkExpect(test2.vertices.get(0).get(0).outEdges.get(0).drawHorizontalEdge(5),
                new RectangleImage(5, 1, OutlineMode.SOLID, Color.black));
        t.checkExpect(test2.vertices.get(0).get(0).outEdges.get(1).drawHorizontalEdge(20),
                new RectangleImage(20, 1, OutlineMode.SOLID, Color.black));
        t.checkExpect(test2.vertices.get(0).get(1).outEdges.get(1).drawHorizontalEdge(10),
                new RectangleImage(10, 1, OutlineMode.SOLID, Color.black));
    }

    // tests the drawVerticalEdge method
    void testDrawVerticalEdge(Tester t) {
        Maze test = new Maze(4, 4, new Random(4));
        t.checkExpect(test.vertices.get(0).get(0).outEdges.get(0).drawVerticalEdge(10),
                new RectangleImage(1, 10, OutlineMode.SOLID, Color.black));
        t.checkExpect(test.vertices.get(0).get(0).outEdges.get(1).drawVerticalEdge(100),
                new RectangleImage(1, 100, OutlineMode.SOLID, Color.black));
        t.checkExpect(test.vertices.get(0).get(1).outEdges.get(1).drawVerticalEdge(100),
                new RectangleImage(1, 100, OutlineMode.SOLID, Color.black));

        Maze test2 = new Maze(6, 6, new Random(4));
        t.checkExpect(test2.vertices.get(0).get(0).outEdges.get(0).drawVerticalEdge(5),
                new RectangleImage(1, 5, OutlineMode.SOLID, Color.black));
        t.checkExpect(test2.vertices.get(0).get(0).outEdges.get(1).drawVerticalEdge(20),
                new RectangleImage(1, 20, OutlineMode.SOLID, Color.black));
        t.checkExpect(test2.vertices.get(0).get(1).outEdges.get(1).drawVerticalEdge(10),
                new RectangleImage(1, 10, OutlineMode.SOLID, Color.black));
    }

    // tests for createVertices method
    void testCreateVertices(Tester t) {
        Maze mazeTest = new Maze(4, 4, new Random(4));
        t.checkExpect(mazeTest.vertices.get(0).get(0).x, 0);
        t.checkExpect(mazeTest.vertices.get(0).get(0).y, 0);
        t.checkExpect(mazeTest.vertices.get(0).get(1).x, 1);
        t.checkExpect(mazeTest.vertices.get(0).get(1).y, 0);
        t.checkExpect(mazeTest.vertices.get(1).get(0).x, 0);
        t.checkExpect(mazeTest.vertices.get(1).get(0).y, 1);
        t.checkExpect(mazeTest.vertices.get(1).get(1).x, 1);
        t.checkExpect(mazeTest.vertices.get(1).get(1).y, 1);

        Maze test2 = new Maze(6, 6, new Random(4));
        t.checkExpect(test2.vertices.get(0).get(0).x, 0);
        t.checkExpect(test2.vertices.get(0).get(0).y, 0);
        t.checkExpect(test2.vertices.get(0).get(1).x, 1);
        t.checkExpect(test2.vertices.get(0).get(1).y, 0);
        t.checkExpect(test2.vertices.get(1).get(0).x, 0);
        t.checkExpect(test2.vertices.get(1).get(0).y, 1);
        t.checkExpect(test2.vertices.get(1).get(1).x, 1);
        t.checkExpect(test2.vertices.get(1).get(1).y, 1);
    }

    // tests for the edgeInTree method
    void testEdgeInTree(Tester t) {

        Maze mazeTest = new Maze(4, 4, new Random(4));
        // checking initial conditions
        t.checkExpect(mazeTest.edgesInTree.size(), 0);
        mazeTest.initializeWorklist();
        mazeTest.createRepresentatives();
        mazeTest.kruskals();
        t.checkExpect(mazeTest.edgeInTree(vert1, vert3), false);
        t.checkExpect(mazeTest.edgeInTree(vert1, vert2), false);
        t.checkExpect(mazeTest.edgeInTree(vert4, vert5), false);
        t.checkExpect(mazeTest.edgeInTree(vert1, vert6), false);
        t.checkExpect(
                mazeTest.edgeInTree(mazeTest.edgesInTree.get(0).to, mazeTest.edgesInTree.get(0).from),
                true);
        t.checkExpect(
                mazeTest.edgeInTree(mazeTest.edgesInTree.get(0).from, mazeTest.edgesInTree.get(0).to),
                true);
        t.checkExpect(
                mazeTest.edgeInTree(mazeTest.edgesInTree.get(0).to, mazeTest.edgesInTree.get(0).to), false);
        t.checkExpect(
                mazeTest.edgeInTree(mazeTest.edgesInTree.get(0).from, mazeTest.edgesInTree.get(0).from),
                false);
        t.checkExpect(
                mazeTest.edgeInTree(mazeTest.edgesInTree.get(0).to, mazeTest.edgesInTree.get(1).from),
                false);
        t.checkExpect(
                mazeTest.edgeInTree(mazeTest.edgesInTree.get(0).from, mazeTest.edgesInTree.get(1).to),
                true);

        Maze test2 = new Maze(6, 6, new Random(4));
        // checking initial conditions
        t.checkExpect(test2.edgesInTree.size(), 0);
        test2.initializeWorklist();
        test2.createRepresentatives();
        test2.kruskals();
        t.checkExpect(test2.edgeInTree(vert1, vert3), false);
        t.checkExpect(test2.edgeInTree(vert1, vert2), false);
        t.checkExpect(test2.edgeInTree(vert4, vert5), false);
        t.checkExpect(test2.edgeInTree(vert1, vert6), false);
        t.checkExpect(test2.edgeInTree(test2.edgesInTree.get(0).to, test2.edgesInTree.get(0).from),
                true);
        t.checkExpect(test2.edgeInTree(test2.edgesInTree.get(0).from, test2.edgesInTree.get(0).to),
                true);
        t.checkExpect(test2.edgeInTree(test2.edgesInTree.get(0).to, test2.edgesInTree.get(0).to),
                false);
        t.checkExpect(test2.edgeInTree(test2.edgesInTree.get(0).from, test2.edgesInTree.get(0).from),
                false);
        t.checkExpect(test2.edgeInTree(test2.edgesInTree.get(0).to, test2.edgesInTree.get(1).from),
                false);
        t.checkExpect(test2.edgeInTree(test2.edgesInTree.get(0).from, test2.edgesInTree.get(1).to),
                false);

    }

    // tests for the createEdges method
    void testCreateEdges(Tester t) {
        Maze mazeTest = new Maze(4, 4, new Random(4));

        t.checkExpect(mazeTest.vertices.get(0).get(0).outEdges.size(), 2);
        t.checkExpect(mazeTest.vertices.get(1).get(0).outEdges.size(), 2);
        t.checkExpect(mazeTest.vertices.get(2).get(2).outEdges.size(), 2);
        t.checkExpect(mazeTest.vertices.get(0).get(1).outEdges.size(), 2);
        t.checkExpect(mazeTest.vertices.get(0).get(0).outEdges.get(0).from,
                mazeTest.vertices.get(0).get(0));
        t.checkExpect(mazeTest.vertices.get(0).get(0).outEdges.get(1).from,
                mazeTest.vertices.get(0).get(0));
        t.checkExpect(mazeTest.vertices.get(1).get(0).outEdges.get(0).from,
                mazeTest.vertices.get(1).get(0));
        t.checkExpect(mazeTest.vertices.get(1).get(0).outEdges.get(0).from,
                mazeTest.vertices.get(1).get(0));
        t.checkExpect(mazeTest.vertices.get(0).get(0).outEdges.get(0).to,
                mazeTest.vertices.get(0).get(1));
        t.checkExpect(mazeTest.vertices.get(0).get(0).outEdges.get(1).to,
                mazeTest.vertices.get(1).get(0));
        t.checkExpect(mazeTest.vertices.get(1).get(0).outEdges.get(0).to,
                mazeTest.vertices.get(1).get(1));
        t.checkExpect(mazeTest.vertices.get(1).get(0).outEdges.get(0).to,
                mazeTest.vertices.get(1).get(1));

        // checking the weights
        t.checkExpect(mazeTest.vertices.get(0).get(0).outEdges.get(1).weight, -396984392);
        t.checkExpect(mazeTest.vertices.get(1).get(0).outEdges.get(0).weight, 1221699692);
    }

    // tests for the makeScene method
    void testMakeScene(Tester t) {
        WorldScene scene = new WorldScene(500, 500);
        Maze mazeTest1 = new Maze(4, 4, new Random(4));
        WorldImage img2 = new RectangleImage(123, 123, OutlineMode.SOLID, Color.white);
        WorldImage img3 = new RectangleImage(125, 1, OutlineMode.SOLID, Color.BLACK);
        WorldImage img5 = new RectangleImage(1, 125, OutlineMode.SOLID, Color.BLACK);
        WorldImage img6 = new RectangleImage(123, 123, OutlineMode.SOLID, Color.white);

        WorldImage imgStart = new RectangleImage(123, 123, OutlineMode.SOLID, Color.pink);
        WorldImage imgEnd = new RectangleImage(123, 123, OutlineMode.SOLID, Color.green);

        scene.placeImageXY(imgStart, 62, 62);
        scene.placeImageXY(img5, 125, 62);
        scene.placeImageXY(img3, 62, 125);
        scene.placeImageXY(img6, 187, 62);
        scene.placeImageXY(img5, 250, 62);
        scene.placeImageXY(img3, 187, 125);
        scene.placeImageXY(img2, 312, 62);
        scene.placeImageXY(img5, 375, 62);
        scene.placeImageXY(img3, 312, 125);
        scene.placeImageXY(img2, 437, 62);
        scene.placeImageXY(img3, 437, 125);
        scene.placeImageXY(img5, 500, 62);
        scene.placeImageXY(img2, 62, 187);
        scene.placeImageXY(img5, 125, 187);
        scene.placeImageXY(img3, 62, 250);
        scene.placeImageXY(img2, 187, 187);
        scene.placeImageXY(img5, 250, 187);
        scene.placeImageXY(img3, 187, 250);
        scene.placeImageXY(img2, 312, 187);
        scene.placeImageXY(img5, 375, 187);
        scene.placeImageXY(img3, 312, 250);
        scene.placeImageXY(img2, 437, 187);
        scene.placeImageXY(img3, 437, 250);
        scene.placeImageXY(img5, 500, 187);
        scene.placeImageXY(img2, 62, 312);
        scene.placeImageXY(img5, 125, 312);
        scene.placeImageXY(img3, 62, 375);
        scene.placeImageXY(img2, 187, 312);
        scene.placeImageXY(img5, 250, 312);
        scene.placeImageXY(img3, 187, 375);
        scene.placeImageXY(img2, 312, 312);
        scene.placeImageXY(img5, 375, 312);
        scene.placeImageXY(img3, 312, 375);
        scene.placeImageXY(img2, 437, 312);
        scene.placeImageXY(img3, 437, 375);
        scene.placeImageXY(img5, 500, 312);
        scene.placeImageXY(img2, 62, 437);
        scene.placeImageXY(img5, 125, 437);
        scene.placeImageXY(img3, 62, 500);
        scene.placeImageXY(img2, 187, 437);
        scene.placeImageXY(img5, 250, 437);
        scene.placeImageXY(img3, 187, 500);
        scene.placeImageXY(img2, 312, 437);
        scene.placeImageXY(img5, 375, 437);
        scene.placeImageXY(img3, 312, 500);
        scene.placeImageXY(imgEnd, 437, 437);
        scene.placeImageXY(img3, 437, 500);
        scene.placeImageXY(img5, 500, 437);

        t.checkExpect(mazeTest1.makeScene(), scene);
    }

    // tests for the method createRepresentatives
    void testCreateRep(Tester t) {
        Maze mazeTest = new Maze(4, 4, new Random(4));
        mazeTest.createRepresentatives();
        t.checkExpect(mazeTest.representatives.size(), 16);
        t.checkExpect(mazeTest.representatives.get(mazeTest.vertices.get(1).get(1)),
                mazeTest.vertices.get(1).get(1));
        t.checkExpect(mazeTest.representatives.get(mazeTest.vertices.get(0).get(1)),
                mazeTest.vertices.get(0).get(1));
        t.checkExpect(mazeTest.representatives.get(mazeTest.vertices.get(2).get(1)),
                mazeTest.vertices.get(2).get(1));

        Maze test2 = new Maze(6, 6, new Random(4));
        test2.createRepresentatives();
        t.checkExpect(test2.representatives.size(), 36);
        t.checkExpect(test2.representatives.get(test2.vertices.get(1).get(1)),
                test2.vertices.get(1).get(1));
        t.checkExpect(test2.representatives.get(test2.vertices.get(0).get(1)),
                test2.vertices.get(0).get(1));
        t.checkExpect(test2.representatives.get(test2.vertices.get(2).get(1)),
                test2.vertices.get(2).get(1));
    }

    // tests for the method initializeWorklist
    void testInitalizeWorklist(Tester t) {

        Maze mazeTest = new Maze(4, 4, new Random(4));

        mazeTest.worklist = new ArrayList<Edge>();
        t.checkExpect(mazeTest.worklist.size(), 0);
        mazeTest.initializeWorklist();
        t.checkExpect(mazeTest.worklist.size(), 24);
        t.checkExpect(mazeTest.worklist.get(0).weight, -2003898889);
        t.checkExpect(mazeTest.worklist.get(1).weight, -1827266343);
        t.checkExpect(mazeTest.worklist.get(2).weight, -1376291473);
        t.checkExpect(mazeTest.worklist.get(23).weight, 2140416855);

        Maze test2 = new Maze(6, 6, new Random(4));

        test2.worklist = new ArrayList<Edge>();
        t.checkExpect(test2.worklist.size(), 0);
        test2.initializeWorklist();
        t.checkExpect(test2.worklist.size(), 60);
        t.checkExpect(test2.worklist.get(0).weight, -2044203967);
        t.checkExpect(test2.worklist.get(1).weight, -2003898889);
        t.checkExpect(test2.worklist.get(2).weight, -1982954707);
        t.checkExpect(test2.worklist.get(23).weight, -633008280);
        t.checkExpect(test2.worklist.get(59).weight, 2140416855);

    }

    // tests for Kruskals method
    void testKruskals(Tester t) {

        Maze mazeTest = new Maze(4, 4, new Random(4));
        // checking initial conditions
        t.checkExpect(mazeTest.edgesInTree.size(), 0);
        t.checkExpect(mazeTest.worklist.size(), 0);

        // intializing worklist and creating representatives
        mazeTest.initializeWorklist();
        t.checkExpect(mazeTest.worklist.size(), 24);
        mazeTest.createRepresentatives();

        // checking conditions after kruskals
        mazeTest.kruskals();
        t.checkExpect(mazeTest.edgesInTree.size(), 15);
        t.checkExpect(mazeTest.worklist.size(), 8);

        Maze test2 = new Maze(6, 6, new Random(4));
        // checking initial conditions
        t.checkExpect(test2.edgesInTree.size(), 0);
        t.checkExpect(test2.worklist.size(), 0);

        // intializing worklist and creating representatives
        test2.initializeWorklist();
        t.checkExpect(test2.worklist.size(), 60);
        test2.createRepresentatives();

        // checking conditions after kruskals
        test2.kruskals();
        t.checkExpect(test2.edgesInTree.size(), 35);
        t.checkExpect(test2.worklist.size(), 3);

    }

    // tests for the onKeyEvent
    void testOnKeyEvent(Tester t) {
        Maze mazeTest = new Maze(4, 4, new Random(4));

        // initialzing world
        mazeTest.initializeWorklist();
        mazeTest.createRepresentatives();
        mazeTest.kruskals();

        // checking initial conditions
        t.checkExpect(mazeTest.cellOn, null);
        t.checkExpect(mazeTest.manual, false);
        t.checkExpect(mazeTest.topLeft.current, false);
        mazeTest.onKeyEvent("m");
        // checking changed conditions
        t.checkExpect(mazeTest.cellOn, mazeTest.topLeft);
        t.checkExpect(mazeTest.manual, true);
        t.checkExpect(mazeTest.topLeft.current, true);

        // checking initial conditions
        t.checkExpect(mazeTest.bfs, false);
        mazeTest.onKeyEvent("b");
        // checking changed conditions
        t.checkExpect(mazeTest.bfs, true);

        // checking initial conditions
        t.checkExpect(mazeTest.dfs, false);
        mazeTest.onKeyEvent("d");
        // checking changed conditions
        t.checkExpect(mazeTest.dfs, true);

        // checking initial conditions
        t.checkExpect(mazeTest.worklist.size(), 8);
        t.checkExpect(mazeTest.worklist.get(0).weight, 106573384);
        t.checkExpect(mazeTest.worklist.get(1).weight, 336672924);
        t.checkExpect(mazeTest.worklist.get(2).weight, 649536939);
        mazeTest.onKeyEventTest("r", new Random(3));
        // checking changed conditions
        t.checkExpect(mazeTest.worklist.size(), 7);
        t.checkExpect(mazeTest.dfs, false);
        t.checkExpect(mazeTest.bfs, false);
        t.checkExpect(mazeTest.worklist.get(0).weight, 288278256);
        t.checkExpect(mazeTest.worklist.get(1).weight, 304908421);
        t.checkExpect(mazeTest.worklist.get(2).weight, 580736275);
    }

    // tests for the method onCurrent
    void testOnCurrent(Tester t) {
        Maze mazeTest = new Maze(4, 4, new Random(4));
        t.checkExpect(mazeTest.vertices.get(0).get(0).current, false);
        t.checkExpect(mazeTest.vertices.get(0).get(1).current, false);
        t.checkExpect(mazeTest.vertices.get(1).get(1).current, false);

        // calling onCurrent
        mazeTest.vertices.get(0).get(0).onCurrent();
        mazeTest.vertices.get(0).get(1).onCurrent();
        mazeTest.vertices.get(1).get(1).onCurrent();

        // checking altered conditions
        t.checkExpect(mazeTest.vertices.get(0).get(0).current, true);
        t.checkExpect(mazeTest.vertices.get(0).get(1).current, true);
        t.checkExpect(mazeTest.vertices.get(1).get(1).current, true);

        Maze test2 = new Maze(6, 6, new Random(4));
        t.checkExpect(test2.vertices.get(0).get(0).current, false);
        t.checkExpect(test2.vertices.get(0).get(1).current, false);
        t.checkExpect(test2.vertices.get(1).get(1).current, false);

        // calling highlight
        test2.vertices.get(0).get(0).onCurrent();
        test2.vertices.get(0).get(1).onCurrent();
        test2.vertices.get(1).get(1).onCurrent();

        // checking altered conditions
        t.checkExpect(test2.vertices.get(0).get(0).current, true);
        t.checkExpect(test2.vertices.get(0).get(1).current, true);
        t.checkExpect(test2.vertices.get(1).get(1).current, true);
    }

    // tests for the method highlight
    void testHighlight(Tester t) {
        Maze mazeTest = new Maze(4, 4, new Random(4));
        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, false);
        t.checkExpect(mazeTest.vertices.get(0).get(1).visited, false);
        t.checkExpect(mazeTest.vertices.get(1).get(1).visited, false);

        // calling highlight
        mazeTest.vertices.get(0).get(0).highlight();
        mazeTest.vertices.get(0).get(1).highlight();
        mazeTest.vertices.get(1).get(1).highlight();

        // checking altered conditions
        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(0).get(1).visited, true);
        t.checkExpect(mazeTest.vertices.get(1).get(1).visited, true);

        Maze test2 = new Maze(6, 6, new Random(4));
        t.checkExpect(test2.vertices.get(0).get(0).visited, false);
        t.checkExpect(test2.vertices.get(0).get(1).visited, false);
        t.checkExpect(test2.vertices.get(1).get(1).visited, false);

        // calling highlight
        test2.vertices.get(0).get(0).highlight();
        test2.vertices.get(0).get(1).highlight();
        test2.vertices.get(1).get(1).highlight();

        // checking altered conditions
        t.checkExpect(test2.vertices.get(0).get(0).visited, true);
        t.checkExpect(test2.vertices.get(0).get(1).visited, true);
        t.checkExpect(test2.vertices.get(1).get(1).visited, true);
    }

    // tests for the method move
    void testMove(Tester t) {
        Maze mazeTest = new Maze(4, 4, new Random(4));
        // initialzing world
        mazeTest.initializeWorklist();
        mazeTest.createRepresentatives();
        mazeTest.kruskals();
        t.checkExpect(mazeTest.edgesInTree.size(), 15);
        // setting world to manual
        mazeTest.onKeyEvent("m");
        mazeTest.cellOn = mazeTest.vertices.get(0).get(0);
        t.checkExpect(mazeTest.cellOn, mazeTest.vertices.get(0).get(0));
        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, false);

        // moving right
        mazeTest.move("right");
        t.checkExpect(mazeTest.cellOn, mazeTest.vertices.get(0).get(0).right);
        t.checkExpect(mazeTest.manual, true);
        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(0).get(0).right.visited, false);
        t.checkExpect(mazeTest.topLeft.current, false);
        t.checkExpect(mazeTest.vertices.get(0).get(0).right.current, true);

        // moving down
        mazeTest.move("down");
        t.checkExpect(mazeTest.cellOn, mazeTest.vertices.get(0).get(1).down);
        t.checkExpect(mazeTest.manual, true);
        t.checkExpect(mazeTest.vertices.get(0).get(1).visited, true);
        t.checkExpect(mazeTest.vertices.get(0).get(1).down.visited, false);
        t.checkExpect(mazeTest.vertices.get(0).get(1).right.current, false);
        t.checkExpect(mazeTest.vertices.get(0).get(1).down.current, true);

        // moving up
        mazeTest.move("up");
        t.checkExpect(mazeTest.cellOn, mazeTest.vertices.get(1).get(1).up);
        t.checkExpect(mazeTest.manual, true);
        t.checkExpect(mazeTest.vertices.get(1).get(1).visited, true);
        t.checkExpect(mazeTest.vertices.get(1).get(1).up.visited, true);
        t.checkExpect(mazeTest.vertices.get(0).get(1).down.current, false);
        t.checkExpect(mazeTest.vertices.get(1).get(1).up.current, true);

        // moving left
        mazeTest.move("left");
        t.checkExpect(mazeTest.cellOn, mazeTest.vertices.get(0).get(1).left);
        t.checkExpect(mazeTest.manual, true);
        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(1).get(1).up.current, false);
        t.checkExpect(mazeTest.vertices.get(0).get(0).current, true);

    }

    // tests for the method addNeightbors
    void testAddNeighbors(Tester t) {
        Maze mazeTest = new Maze(4, 4, new Random(4));

        ICollection<Vertex> worklist = new Queue<Vertex>();
        ArrayList<Edge> edgesInSpan = new ArrayList<Edge>();

        edgesInSpan.add(new Edge(mazeTest.vertices.get(0).get(0),
                mazeTest.vertices.get(3).get(3), 0));
        t.checkExpect(worklist.isEmpty(), true);
        mazeTest.vertices.get(0).get(0).addNeighbors(worklist, edgesInSpan);
        // should not change the worklist
        t.checkExpect(worklist.isEmpty(), true);

        edgesInSpan.add(new Edge(mazeTest.vertices.get(0).get(0),
                mazeTest.vertices.get(0).get(0).down, 0));
        mazeTest.vertices.get(0).get(0).addNeighbors(worklist, edgesInSpan);
        t.checkExpect(worklist.isEmpty(), false);
        t.checkExpect(worklist.size(), 1);

        // checking initial
        t.checkExpect(mazeTest.vertices.get(1).get(1).visited, false);
        // changing conditions, should change the worklist
        edgesInSpan.add(new Edge(mazeTest.vertices.get(1).get(1),
                mazeTest.vertices.get(1).get(1).right, 0));
        mazeTest.vertices.get(1).get(1).addNeighbors(worklist, edgesInSpan);
        t.checkExpect(worklist.isEmpty(), false);
        t.checkExpect(mazeTest.vertices.get(1).get(1).visited, true);
        t.checkExpect(worklist.size(), 2);

        // testing left if branch
        edgesInSpan.add(new Edge(mazeTest.vertices.get(2).get(2),
                mazeTest.vertices.get(2).get(2).left, 0));
        mazeTest.vertices.get(2).get(2).addNeighbors(worklist, edgesInSpan);
        t.checkExpect(mazeTest.vertices.get(2).get(2).visited, true);
        t.checkExpect(worklist.size(), 3);

        // testing up if branch
        edgesInSpan.add(new Edge(mazeTest.vertices.get(3).get(3),
                mazeTest.vertices.get(3).get(3).up, 0));
        mazeTest.vertices.get(3).get(3).addNeighbors(worklist, edgesInSpan);
        t.checkExpect(mazeTest.vertices.get(3).get(3).visited, true);
        t.checkExpect(worklist.size(), 4);

    }

    // tests for the union method
    void testUnion(Tester t) {

        Maze mazeTest = new Maze(4, 4, new Random(4));
        // checking inital conditions
        t.checkExpect(mazeTest.representatives.size(), 0);

        // changing conditions
        mazeTest.union(mazeTest.vertices.get(0).get(0), mazeTest.vertices.get(0).get(0));
        // checking changes
        t.checkExpect(mazeTest.representatives.size(), 1);
        t.checkExpect(mazeTest.representatives.get(mazeTest.vertices.get(0).get(0)),
                mazeTest.vertices.get(0).get(0));
        // changing conditions
        mazeTest.union(mazeTest.vertices.get(1).get(1), mazeTest.vertices.get(2).get(2));
        t.checkExpect(mazeTest.representatives.size(), 2);
        t.checkExpect(mazeTest.representatives.get(mazeTest.vertices.get(1).get(1)),
                mazeTest.vertices.get(2).get(2));

        Maze test2 = new Maze(6, 6, new Random(4));
        // checking inital conditions
        t.checkExpect(test2.representatives.size(), 0);

        // changing conditions
        test2.union(test2.vertices.get(0).get(0), test2.vertices.get(0).get(0));
        // checking changes
        t.checkExpect(test2.representatives.size(), 1);
        t.checkExpect(test2.representatives.get(test2.vertices.get(0).get(0)),
                test2.vertices.get(0).get(0));
        // changing conditions
        test2.union(test2.vertices.get(1).get(1), test2.vertices.get(2).get(2));
        t.checkExpect(test2.representatives.size(), 2);
        t.checkExpect(test2.representatives.get(test2.vertices.get(1).get(1)),
                test2.vertices.get(2).get(2));
    }

    // tests for the find method
    void testFind(Tester t) {
        // initialzing a new world
        Maze mazeTest = new Maze(4, 4, new Random(4));
        mazeTest.initializeWorklist();
        mazeTest.createRepresentatives();

        t.checkExpect(mazeTest.find(mazeTest.vertices.get(0).get(0)), mazeTest.vertices.get(0).get(0));
        t.checkExpect(mazeTest.find(mazeTest.vertices.get(3).get(3)), mazeTest.vertices.get(3).get(3));
        t.checkExpect(mazeTest.find(mazeTest.vertices.get(1).get(1)), mazeTest.vertices.get(1).get(1));

        // initializing a new world
        Maze test2 = new Maze(6, 6, new Random(4));
        test2.initializeWorklist();
        test2.createRepresentatives();
        t.checkExpect(test2.find(test2.vertices.get(0).get(0)), test2.vertices.get(0).get(0));
        t.checkExpect(test2.find(test2.vertices.get(3).get(3)), test2.vertices.get(3).get(3));
        t.checkExpect(test2.find(test2.vertices.get(1).get(1)), test2.vertices.get(1).get(1));

    }

    // tests for the method step
    void testStep(Tester t) {
        // creating initial conditions
        Maze mazeTest = new Maze(4, 4, new Random(4));

        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, false);
        t.checkExpect(mazeTest.vertices.get(0).get(1).visited, false);
        t.checkExpect(mazeTest.vertices.get(1).get(0).visited, false);
        t.checkExpect(mazeTest.vertices.get(2).get(0).visited, false);

        mazeTest.initializeWorklist();
        mazeTest.createRepresentatives();
        mazeTest.kruskals();

        // changing conditions by stepping
        mazeTest.bfsCur.step(mazeTest.edgesInTree);
        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(0).get(1).visited, false);
        t.checkExpect(mazeTest.vertices.get(1).get(0).visited, false);
        t.checkExpect(mazeTest.vertices.get(2).get(0).visited, false);
        t.checkExpect(mazeTest.bfsCur.alreadySeen.size(), 1);

        mazeTest.bfsCur.step(mazeTest.edgesInTree);
        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(0).get(1).visited, false);
        t.checkExpect(mazeTest.vertices.get(1).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(2).get(0).visited, false);
        t.checkExpect(mazeTest.bfsCur.alreadySeen.size(), 2);

        mazeTest.bfsCur.step(mazeTest.edgesInTree);
        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(0).get(1).visited, true);
        t.checkExpect(mazeTest.vertices.get(1).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(2).get(0).visited, false);
        t.checkExpect(mazeTest.bfsCur.alreadySeen.size(), 3);
    }

    // tests for the onTick method
    void testOnTick(Tester t) {
        // creating and checking initial conditions
        Maze mazeTest = new Maze(4, 4, new Random(4));

        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, false);
        t.checkExpect(mazeTest.vertices.get(0).get(1).visited, false);
        t.checkExpect(mazeTest.vertices.get(1).get(0).visited, false);
        t.checkExpect(mazeTest.vertices.get(2).get(0).visited, false);

        mazeTest.initializeWorklist();
        mazeTest.createRepresentatives();
        mazeTest.kruskals();
        mazeTest.bfs = true;

        // calling onTick
        mazeTest.onTick();
        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(0).get(1).visited, false);
        t.checkExpect(mazeTest.vertices.get(1).get(0).visited, false);
        t.checkExpect(mazeTest.vertices.get(2).get(0).visited, false);
        // calling onTick
        mazeTest.onTick();
        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(0).get(1).visited, false);
        t.checkExpect(mazeTest.vertices.get(1).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(2).get(0).visited, false);
        // calling onTick
        mazeTest.onTick();
        t.checkExpect(mazeTest.vertices.get(0).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(0).get(1).visited, true);
        t.checkExpect(mazeTest.vertices.get(1).get(0).visited, true);
        t.checkExpect(mazeTest.vertices.get(2).get(0).visited, false);

        Maze test2 = new Maze(6, 6, new Random(4));
        test2.initializeWorklist();
        test2.createRepresentatives();
        test2.kruskals();
        test2.dfs = true;

        t.checkExpect(test2.vertices.get(0).get(0).visited, false);
        t.checkExpect(test2.vertices.get(0).get(1).visited, false);
        t.checkExpect(test2.vertices.get(1).get(0).visited, false);
        t.checkExpect(test2.vertices.get(2).get(0).visited, false);

        // calling onTick
        test2.onTick();
        t.checkExpect(test2.vertices.get(0).get(0).visited, true);
        t.checkExpect(test2.vertices.get(0).get(1).visited, false);
        t.checkExpect(test2.vertices.get(1).get(0).visited, false);
        t.checkExpect(test2.vertices.get(2).get(0).visited, false);
        // calling onTick
        test2.onTick();
        t.checkExpect(test2.vertices.get(0).get(0).visited, true);
        t.checkExpect(test2.vertices.get(0).get(1).visited, true);
        t.checkExpect(test2.vertices.get(1).get(0).visited, false);
        t.checkExpect(test2.vertices.get(2).get(0).visited, false);
        // calling onTick
        test2.onTick();
        test2.onTick();
        test2.onTick();
        test2.onTick();
        test2.onTick();
        t.checkExpect(test2.vertices.get(0).get(0).visited, true);
        t.checkExpect(test2.vertices.get(0).get(1).visited, true);
        t.checkExpect(test2.vertices.get(0).get(2).visited, true);
        t.checkExpect(test2.vertices.get(0).get(3).visited, true);
    }

    // tests for the equals method
    void testEquals(Tester t) {
        Maze mazeTest = new Maze(4, 4, new Random(4));
        mazeTest.initializeWorklist();
        mazeTest.createRepresentatives();
        mazeTest.kruskals();

        t.checkExpect(mazeTest.edgesInTree.get(0).equals(mazeTest.edgesInTree.get(0)), true);
        t.checkExpect(mazeTest.edgesInTree.get(0).equals(mazeTest.edgesInTree.get(1)), false);
        t.checkExpect(mazeTest.edgesInTree.get(0).equals(mazeTest.vertices.get(0).get(0)), false);
        t.checkExpect(mazeTest.edgesInTree.get(0).equals(mazeTest.edgesInTree.get(4)), false);
        t.checkExpect(mazeTest.edgesInTree.get(4).equals(mazeTest.edgesInTree.get(4)), true);
    }

    // tests for the method compareTo
    void testCompareTo(Tester t) {
        t.checkExpect(this.edge1.compareTo(edge1), 0);
        t.checkExpect(this.edge4.compareTo(edge1), 1);
        t.checkExpect(this.edge1.compareTo(edge3), -1);
    }
}