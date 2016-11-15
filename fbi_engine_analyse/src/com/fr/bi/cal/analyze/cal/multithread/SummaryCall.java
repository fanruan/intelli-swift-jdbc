package com.fr.bi.cal.analyze.cal.multithread;import com.fr.bi.cal.analyze.cal.result.Node;import com.fr.bi.cal.analyze.cal.sssecret.NoneDimensionGroup;import com.fr.bi.stable.report.result.TargetCalculator;import java.util.concurrent.Callable;/** * Created by Hiram on 2015/5/14. */public class SummaryCall implements Callable<Object> {    private Node node;    private NoneDimensionGroup noneDimensionGroup;    private TargetCalculator calculator;    public SummaryCall(Node node, NoneDimensionGroup noneDimensionGroup, TargetCalculator calculator) {        this.node = node;        this.noneDimensionGroup = noneDimensionGroup;        this.calculator = calculator;    }    @Override    public Object call() throws Exception {        if (calculator != null){            Number v = noneDimensionGroup.getSummaryValue(calculator);            if (v != null) {                node.setTargetGetter(calculator.createTargetGettingKey(), noneDimensionGroup.getRoot().getGroupValueIndex());                node.setTargetIndex(calculator.createTargetGettingKey(), noneDimensionGroup.getRoot().getGroupValueIndex());                node.setSummaryValue(calculator.createTargetGettingKey(), v);            }        }        return null;    }}