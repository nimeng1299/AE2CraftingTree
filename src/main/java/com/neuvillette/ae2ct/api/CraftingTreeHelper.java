package com.neuvillette.ae2ct.api;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import com.neuvillette.ae2ct.Config;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CraftingTreeHelper {
    private RecipeHelper recipeHelper;
    private List<CraftingPlanSummaryEntry> entries;
    private Map<AEKey, RecipeHelper.Recipe> cache = new HashMap<>();
    private Map<AEKey, AmountHelper> amountCache = new HashMap<>();
    private Map<Point, Node> nodesMap = new HashMap<>();

    private int max_x = 0;
    private int max_y = 0;

    public boolean now_mode = true; // true = compact
    public CraftingTreeHelper(RecipeHelper recipeHelper, List<CraftingPlanSummaryEntry> entries) {
        this.recipeHelper = recipeHelper;
        this.entries = entries;
    }

    public NodeManager build(){
        if (recipeHelper == null) return null;

        cache.clear();
        nodesMap.clear();
        var recipes = recipeHelper.recipes;
        for(var recipe : recipes){
            var output = recipe.outputs().get(0).what();
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
                outputamount = v.outputs().get(0).amount();
                times = amount / outputamount;
                if(amount % outputamount != 0) times++;
                inputs = v.inputs();
                break;
            }
        }
        max_x = 0;
        max_y = 0;
        var node = buildNode(output, amount, inputs, times, outputamount);
        NodeManager nodeManager = new NodeManager(node);
        nodeManager.nodeSetPoint(node, new Point(0, 0));
        buildNodePosition(node, nodeManager);
        return nodeManager;
    }

    public void buildNodePosition(Node node, NodeManager nodeManager){
        nodeManager.map.clear();
        if (Config.USE_COMPACT_TREE.get()){
            now_mode = true;
            buildCompactNode(node, nodeManager, new Point(0, 0));
        }else{
            now_mode = false;
            bulidLooseNode(node, nodeManager, new Point(0, 0), 1);
        }
    }

    public Node buildNode(GenericStack stack, Long amount, List<GenericStack> inputs, long times, long outputamount){
        if(cache == null || cache.isEmpty() || amountCache == null || amountCache.isEmpty()) return null;
        List<Node> nodes = new ArrayList<>();
        var amoCache = amountCache.get(stack.what());
        if(amoCache == null) amoCache = new AmountHelper(0, Long.MAX_VALUE, Long.MAX_VALUE);
        if(inputs.isEmpty()){
            var storedAmo = AmountHelper.check(amount - amoCache.missingAmount);
            AmountHelper a = new AmountHelper(AmountHelper.check(amount - storedAmo), storedAmo, 0);
            amountCache.put(stack.what(), new AmountHelper(AmountHelper.check(amoCache.missingAmount - amount), amoCache.storedAmount, amoCache.craftAmount));
            Node n =  new Node(stack, amount, a);
            return n;
        }
        if(times == 0)
        {
            AmountHelper a = new AmountHelper(0, amount, 0);
            amountCache.put(stack.what(), new AmountHelper(amoCache.missingAmount, AmountHelper.check(amoCache.storedAmount - amount), amoCache.craftAmount));
            Node n =  new Node(stack, amount, a);
            return n;
        }
        for(var input : inputs){
            var amo = input.amount() * times;
            long t = 0;
            long a = 0;
            List<GenericStack> ins = new ArrayList<>();
            for (Map.Entry<AEKey, RecipeHelper.Recipe> entry : cache.entrySet()) {
                AEKey k = entry.getKey();
                RecipeHelper.Recipe v = entry.getValue();
                if (k.equals(input.what())) {
                    a = v.outputs().get(0).amount();
                    var needcraft = AmountHelper.check(amo - amountCache.get(input.what()).storedAmount);
                    t = needcraft  / a;
                    if (needcraft % a != 0) t++;
                    ins = v.inputs();
                    break;
                }
            }
            Node n = buildNode(input, amo, ins, t, a);
            nodes.add(n);
        }
        Node n = new Node(stack, amount, new AmountHelper(0, AmountHelper.check(amount - times * outputamount), times * outputamount), nodes);
        return n;
    }

    public int buildCompactNode(Node node, NodeManager manager, Point initialPoint) {
        // 当前节点的最终位置
        Point currentPos = new Point(initialPoint.x, initialPoint.y);
        int totalShift = 0;

        // 阶段1：确定当前节点初始位置（根节点直接设置）
        if (!manager.root.equals(node)) {
            // 非根节点寻找可用位置
            while (manager.map.containsKey(currentPos)) {
                currentPos.x++;
                totalShift++;
            }
            updateNodePosition(node, manager, currentPos);
        } else {
            updateNodePosition(node, manager, currentPos);
        }

        // 叶子节点直接返回
        if (node.subNodes.isEmpty()) return totalShift;

        // 阶段2：递归处理所有子节点
        List<Point> childPositions = new ArrayList<>();
        int maxChildShift = 0;

        // 先处理所有子节点以收集最终位置
        for (int i = 0; i < node.subNodes.size(); i++) {
            Node child = node.subNodes.get(i);
            Point childInitial = new Point(currentPos.x + i, currentPos.y + 1);
            int childShift = buildCompactNode(child, manager, childInitial);

            // 记录子节点最终位置
            childPositions.add(child.point);
            maxChildShift = Math.max(maxChildShift, childShift);
        }

        // 阶段3：根据第一个子节点调整父节点位置
        if (!childPositions.isEmpty()) {
            Point firstChildPos = childPositions.get(0);
            Point targetParentPos = new Point(firstChildPos.x, currentPos.y);

            // 解决父节点位置冲突
            while (manager.map.containsKey(targetParentPos)) {
                targetParentPos.x++;
                totalShift++;
            }

            // 移动父节点到第一个子节点正上方
            if (!targetParentPos.equals(currentPos)) {
                updateNodePosition(node, manager, targetParentPos);
                totalShift += (targetParentPos.x - currentPos.x);
                currentPos = targetParentPos;
            }

            // 阶段4：重新排列子节点保持紧凑
            int baseX = currentPos.x;
            for (int i = 0; i < node.subNodes.size(); i++) {
                Node child = node.subNodes.get(i);
                Point desiredPos = new Point(baseX + i, currentPos.y + 1);

                // 仅当需要移动时才更新
                if (!child.point.equals(desiredPos)) {
                    // 释放旧位置
                    manager.map.remove(child.point);
                    // 确保新位置可用
                    while (manager.map.containsKey(desiredPos)) {
                        desiredPos.x++;
                        totalShift++;
                    }
                    updateNodePosition(child, manager, desiredPos);
                }
            }
        }

        return totalShift;
    }

    private void updateNodePosition(Node node, NodeManager manager, Point newPos) {
        if (node.point != null) {
            manager.map.remove(node.point);
        }
        manager.nodeSetPoint(node, newPos);
    }

    public int bulidLooseNode(Node node, NodeManager manager, Point point, int len){
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
            l += bulidLooseNode(subNode, manager, new Point(x, y), l);
        }
        manager.nodeSetPoint(node, new Point(x, y));
        return l;
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
