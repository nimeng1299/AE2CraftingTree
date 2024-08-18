package com.neuvillette.ae2ct.api;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingTreeHelper {
    private RecipeHelper recipeHelper;
    private Map<AEKey, RecipeHelper.Recipe> cache = new HashMap<>();
    private Map<Point, Node> nodesMap = new HashMap<>();
    public CraftingTreeHelper(RecipeHelper recipeHelper) {
        this.recipeHelper = recipeHelper;
    }

    public Node buildNode(){
        if (recipeHelper == null) return null;

        cache.clear();
        nodesMap.clear();
        var recipes = recipeHelper.recipes;
        for(var recipe : recipes){
            var output = recipe.outputs().getFirst().what();
            if (!cache.containsKey(output)){
                cache.put(output, recipe);
            }
        }

        var output = recipeHelper.output;
        var amount = output.amount();
        long times = 0;
        List<GenericStack> inputs = new ArrayList<>();
        for (Map.Entry<AEKey, RecipeHelper.Recipe> entry : cache.entrySet()) {
            AEKey k = entry.getKey();
            RecipeHelper.Recipe v = entry.getValue();
            if (k.equals(output.what())) {
                long a = v.outputs().getFirst().amount();
                times = amount / a;
                if(amount % a != 0) times++;
                inputs = v.inputs();
                break;
            }
        }

        return build(output, amount, new Point(0, -1), inputs, times, 0);
    }

    private Node build(GenericStack stack, Long amount, Point position,List<GenericStack> inputs, long times, int len){
        //check
        if(cache == null || cache.isEmpty()) return null;
        int x = position.x + len;
        int y = position.y + 1;
        int l = 0;
        List<Node> nodes = new ArrayList<>();
        if(inputs.isEmpty()){
            Node n =  new Node(stack, amount, new Point(x, y), null, 1);
            nodesMap.put(n.position(), n);
            return n;
        }
        for(var input : inputs){
            var amo = input.amount() * times;
            long t = 0;
            List<GenericStack> ins = new ArrayList<>();
            for (Map.Entry<AEKey, RecipeHelper.Recipe> entry : cache.entrySet()) {
                AEKey k = entry.getKey();
                RecipeHelper.Recipe v = entry.getValue();
                if (k.equals(input.what())) {
                    long a = v.outputs().getFirst().amount();
                    t = amo / a;
                    if (amo % a != 0) t++;
                    ins = v.inputs();
                    break;
                }
            }
            Node n = build(input, amo, new Point(x, y), ins, t, l);
            l = n.len() + l;
            nodes.add(n);
        }

        Node n = new Node(stack, amount, new Point(x, y), nodes, l);
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
            int len
    ){

    }
}
