package com.neuvillette.ae2ct.api;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import com.neuvillette.ae2ct.Config;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class CraftingTreeHelper {
    private RecipeHelper recipeHelper;
    private List<CraftingPlanSummaryEntry> entries;
    private Map<AEKey, RecipeHelper.Recipe> cache = new HashMap<>();
    private Map<AEKey, AmountHelper> amountCache = new HashMap<>();

    private Node root = null;
    private Node missingOnlyRoot = null;

    private String searchString = "";
    private int max_x = 0;
    private int max_y = 0;

    public boolean now_mode = true; // true = compact
    public CraftingTreeHelper(RecipeHelper recipeHelper, List<CraftingPlanSummaryEntry> entries) {
        this.recipeHelper = recipeHelper;
        this.entries = entries;
    }

    public NodeManager build(boolean isMissingOnly){
        var node = isMissingOnly ? missingOnlyRoot : root;
        if(node == null) {
            return null;
        }
        NodeManager nodeManager = new NodeManager(node);
        nodeManager.nodeSetPoint(node, new Point(0, 0));
        buildNodePosition(node, nodeManager);
        return nodeManager;
    }

    public void preBuild() {
        if (recipeHelper == null) return;

        cache.clear();
        var recipes = recipeHelper.recipes;
        for(var recipe : recipes){
            var output = recipe.outputs().getFirst().what();
            if (!cache.containsKey(output)){
                cache.put(output, recipe);
            }
        }

        for(var entry : entries){
            var key = entry.getWhat();
            if(!amountCache.containsKey(key)){
                amountCache.put(key, new AmountHelper(entry.getMissingAmount(), entry.getStoredAmount(), entry.getCraftAmount()));
            }
        }

        var output = recipeHelper.output;
        var amount = output.amount();
        long outputamount = 0;
        long times = 0;
        List<GenericStack> inputs = new ArrayList<>();
        for (Map.Entry<AEKey, RecipeHelper.Recipe> entry : cache.entrySet()) {
            AEKey k = entry.getKey();
            RecipeHelper.Recipe v = entry.getValue();
            if (k.equals(output.what())) {
                outputamount = v.outputs().getFirst().amount();
                times = amount / outputamount;
                if(amount % outputamount != 0) times++;
                inputs = v.inputs();
                break;
            }
        }
        max_x = 0;
        max_y = 0;
        root = buildNode(output, amount, inputs, times, outputamount, null);
        missingOnlyRoot = missingOnly(root);
    }

    private Node missingOnly(Node node){
        if(node == null) {
            return null;
        }

        List<Node> newSubNodes = new ArrayList<>();
        for(Node subNode : node.subNodes) {
            Node newSubNode = missingOnly(subNode);
            if(newSubNode != null) {
                newSubNodes.add(newSubNode);
            }
        }
        Node t = new Node(node.stack, node.amount, node.amountHelper, newSubNodes);
        t.parent = node.parent;
        for(Node subNode : newSubNodes) {
            subNode.parent = t;
        }
        if (t.amountHelper.missingAmount <= 0 && t.subNodes.isEmpty()) {
            return null;
        }

        return t;
    }

    public void buildNodePosition(Node node, NodeManager nodeManager){
        nodeManager.map.clear();
        if (Config.USE_COMPACT_TREE.get()){
            now_mode = true;
            buildCompactNode(node, nodeManager, new Point(0, 0));
        }else{
            now_mode = false;
            buildLooseNode(node, nodeManager, new Point(0, 0), 1);
        }
    }

    public Node buildNode(GenericStack stack, Long amount, List<GenericStack> inputs, long times, long outputamount, Node parent){
        //check
        if(cache == null || cache.isEmpty() || amountCache == null || amountCache.isEmpty()) return null;
        var amoCache = amountCache.get(stack.what());
        if(amoCache == null) amoCache = new AmountHelper(0, Long.MAX_VALUE, Long.MAX_VALUE);
        if(inputs.isEmpty()){
            var storedAmo = AmountHelper.check(amount - amoCache.missingAmount);
            AmountHelper a = new AmountHelper(AmountHelper.check(amount - storedAmo), storedAmo, 0);
            amountCache.put(stack.what(), new AmountHelper(AmountHelper.check(amoCache.missingAmount - amount), amoCache.storedAmount, amoCache.craftAmount));
            Node n =  new Node(stack, amount, a);
            n.parent = parent;
            return n;
        }
        if(times == 0)
        {
            AmountHelper a = new AmountHelper(0, amount, 0);
            amountCache.put(stack.what(), new AmountHelper(amoCache.missingAmount, AmountHelper.check(amoCache.storedAmount - amount), amoCache.craftAmount));
            Node n = new Node(stack, amount, a);
            n.parent = parent;
            return n;
        }
        Node n = new Node(stack, amount, new AmountHelper(0, AmountHelper.check(amount - times * outputamount), times * outputamount));
        for(var input : inputs){
            var amo = input.amount() * times;
            long t = 0;
            long a = 0;
            List<GenericStack> ins = new ArrayList<>();
            for (Map.Entry<AEKey, RecipeHelper.Recipe> entry : cache.entrySet()) {
                AEKey k = entry.getKey();
                RecipeHelper.Recipe v = entry.getValue();
                if (k.equals(input.what())) {
                    a = v.outputs().getFirst().amount();
                    var needcraft = AmountHelper.check(amo - amountCache.get(input.what()).storedAmount);
                    t = needcraft  / a;
                    if (needcraft % a != 0) t++;
                    ins = v.inputs();
                    break;
                }
            }
            Node node = buildNode(input, amo, ins, t, a, n);
            n.subNodes.add(node);
        }
        n.parent = parent;
        return n;

    }

    public int buildCompactNode(Node node, NodeManager manager, Point initialPoint) {
        int len = 0;
        Point p = initialPoint;
        while (manager.map.containsKey(p)){
            len++;
            p = new Point(p.x + 1, p.y);
        }
        if(node.subNodes.isEmpty())
        {
            manager.nodeSetPoint(node, p);
            return len;
        }else {
            int addlen = 0;
            int start = 0;
            int end = 0;
            for (int i = 0; i < node.subNodes.size(); i++) {
                var subNode = node.subNodes.get(i);
                addlen += buildCompactNode(subNode, manager, new Point(p.x + i, p.y + 1));
                if(i == 0){
                    p.x += addlen;
                    start = subNode.point.x;
                } else if (i == node.subNodes.size() - 1){
                    end = subNode.point.x;
                }
            }
            for(int i = start; i < end; i++){
                if(!manager.map.containsKey(new Point(i, p.y + 1))){
                    manager.map.put(new Point(i, p.y + 1), null);
                }
            }
            manager.nodeSetPoint(node, p);
            return len;
        }
    }

    public int buildLooseNode(Node node, NodeManager manager, Point point, int len){
        int x = point.x + len;
        int y = point.y + 1;
        if (x > max_x) max_x = x;
        if (y > max_y) max_y = y;
        int l = 0;
        if(node.subNodes.isEmpty()){
            manager.nodeSetPoint(node, new Point(x, y));
            return 1;
        }
        for(var subNode : node.subNodes){
            l += buildLooseNode(subNode, manager, new Point(x, y), l);
        }
        manager.nodeSetPoint(node, new Point(x, y));
        return l;
    }


    public void setSearchString(String searchString) {
        if(!this.searchString.equals(searchString)) {
            this.searchString = searchString;
        }
    }

    public List<Node> search(Node root) {
        if(root == null || searchString.isEmpty()) {
            return null;
        }

        List<Node> selected = new ArrayList<>();
        Stack<Node> stack = new Stack<>();
        stack.push(root);

        while(!stack.isEmpty()) {
            Node node = stack.pop();
            if(node.stack.what().getDisplayName().getString().toLowerCase().contains(searchString)) {
                selected.add(node);
            }

            for(int i = node.subNodes.size() - 1; i >= 0; i--) {
                stack.push(node.subNodes.get(i));
            }
        }

        return selected;
    }

    public class NodeManager{
        public Node root;
        int len;
        public Map<Point, Node> map = new HashMap<>();

        public int max_x = 0;
        public int max_y = 0;


        public NodeManager(Node node){
            this.root = node;
            this.len = 0;
        }
        public void nodeSetPoint(Node node, Point p){
            node.setPoint(p);
            map.put(p, node);
            if (p.x + 1 > max_x) max_x = p.x + 1;
            if (p.y + 1 > max_y) max_y = p.y + 1;
        }

    }

    public class Node{
        public GenericStack stack;
        public Long amount;
        public AmountHelper amountHelper;

        public Point point;
        public List<Node> subNodes;
        public Node parent;

        public Node(GenericStack stack, Long amount, AmountHelper amountHelper){
            this.stack = stack;
            this.amount = amount;
            this.amountHelper = amountHelper;
            this.subNodes = new ArrayList<>();
        }

        public Node(GenericStack stack, Long amount, AmountHelper amountHelper, List<Node> subNodes){
            this.stack = stack;
            this.amount = amount;
            this.amountHelper = amountHelper;
            this.subNodes = subNodes;
        }

        public void setPoint(Point p){
            this.point = p;
        }

    }

    public static class AmountHelper{
        public long missingAmount;
        public long storedAmount;
        public long craftAmount;

        public AmountHelper(long missingAmount, long storedAmount, long craftAmount){
            this.missingAmount = missingAmount;
            this.storedAmount = storedAmount;
            this.craftAmount = craftAmount;
        }

        public static long check(long a) {
            if (a < 0){
                return 0;
            }else {
                return a;
            }
        }
    }
}
