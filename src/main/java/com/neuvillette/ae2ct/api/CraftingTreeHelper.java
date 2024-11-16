package com.neuvillette.ae2ct.api;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingTreeHelper {
    private RecipeHelper recipeHelper;
    private List<CraftingPlanSummaryEntry> entries;
    private Map<AEKey, RecipeHelper.Recipe> cache = new HashMap<>();
    private Map<AEKey, AmountHelper> amountCache = new HashMap<>();
    private Map<Point, Node> nodesMap = new HashMap<>();

    private int max_x = 0;
    private int max_y = 0;
    public CraftingTreeHelper(RecipeHelper recipeHelper, List<CraftingPlanSummaryEntry> entries) {
        this.recipeHelper = recipeHelper;
        this.entries = entries;
    }

    public NodeInfo buildNode(){
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
        Node node =  build(output, amount, new Point(0, -1), inputs, times, 0, outputamount);
        return new NodeInfo(node, max_x + 1, max_y + 1);
    }

    private Node build(GenericStack stack, Long amount, Point position,List<GenericStack> inputs, long times, int len, long outputamount){
        //check
        if(cache == null || cache.isEmpty() || amountCache == null || amountCache.isEmpty()) return null;
        int x = position.x + len;
        int y = position.y + 1;
        if (x > max_x) max_x = x;
        if (y > max_y) max_y = y;
        int l = 0;
        List<Node> nodes = new ArrayList<>();
        var amoCache = amountCache.get(stack.what());
        if(inputs.isEmpty()){
            var storedAmo = AmountHelper.check(amount - amoCache.missingAmount);
            AmountHelper a = new AmountHelper(AmountHelper.check(amount - storedAmo), storedAmo, 0);
            amountCache.put(stack.what(), new AmountHelper(AmountHelper.check(amoCache.missingAmount - amount), amoCache.storedAmount, amoCache.craftAmount));
            Node n =  new Node(stack, amount, new Point(x, y), null, 1, a);
            nodesMap.put(n.position(), n);
            return n;
        }
        if(times == 0)
        {
            AmountHelper a = new AmountHelper(0, amount, 0);
            amountCache.put(stack.what(), new AmountHelper(amoCache.missingAmount, AmountHelper.check(amoCache.storedAmount - amount), amoCache.craftAmount));
            Node n =  new Node(stack, amount, new Point(x, y), null, 1, a);
            nodesMap.put(n.position(), n);
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
            Node n = build(input, amo, new Point(x, y), ins, t, l, a);
            l = n.len() + l;
            nodes.add(n);
        }

        Node n = new Node(stack, amount, new Point(x, y), nodes, l, new AmountHelper(0, AmountHelper.check(amount - times * outputamount), times * outputamount));
        nodesMap.put(n.position(), n);
        return n;

    }

    public Map<Point, Node> getNodesMap() {
        return nodesMap;
    }

    public record Node(
            GenericStack stack,
            Long amount,
            Point position,
            List<Node> subNodes,
            int len,
            AmountHelper amountHelper
    ){

    }
    public record NodeInfo (
            Node node,
            int max_x,
            int max_y){
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
