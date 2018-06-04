import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

import javax.swing.text.StyledEditorKit.UnderlineAction;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

public class main {
	
	private static final int numOfAbstractValues = AbstractValue.values().length;
	private static final double multiplier = 20;
	private static ArrayList<String> importanceRange;
	private static Queue<String>  queue;
	
	public static void main(String[] args) {
		Vector<ArrayList<String>> agentsValues = new Vector<>();
		String inputThresholds = "UNIVERSALISM,30;POWER,50;SELFDIRECTION,30;TRADITION,70";
		for(int numOfAgents = 0; numOfAgents < 100; numOfAgents++){
			importanceRange = new ArrayList<String>();
			System.out.println("\nagent #" + numOfAgents);
			//			Map<String, Double> gValueTrees = createRandomSet();
			Map<String, Double> gValueTrees = createInitialSet(inputThresholds);
			valueFrameworkConditionCheck(gValueTrees);
			sortImportanceRange();
			agentsValues.add(importanceRange);
		}
		statisticsOverAgents(agentsValues);
	}

private static void statisticsOverAgents(
			Vector<ArrayList<String>> agentsValues) {
		int[] agentCounts = new int[4]; //0:UNIVERSALISM, 1:POWER, 2:SELFDIRECTION, 3:TRADITION
//		agentCounts = [0;0;0;0];
		Iterator it = agentsValues.iterator();
		while(it.hasNext()){
			ArrayList<String> element = (ArrayList<String>)it.next();
			switch (getValueName(element.get(0))) {
			case "UNIVERSALISM":
				agentCounts[0]++;
				break;
			case "POWER":
				agentCounts[1]++;
				break;
			case "SELFDIRECTION":
				agentCounts[2]++;
				break;
			case "TRADITION":
				agentCounts[3]++;
				break;
			default:
				System.err.println("unexpected value name! ");
				break;
			}
		}
		
		System.out.println("number agents who value UNIVERSALISM : " + agentCounts[0]+ ", POWER : " + agentCounts[1]+ " ,SELFDIRECTION : " + agentCounts[2] + ", TRADITION : " + agentCounts[3]);
		
	}

private static Map<String, Double> createInitialSet(String inputThresholds) {
	Map<String, Double> gValueTrees = new HashMap<String, Double>();
	String[] inputValueswithThresholds = inputThresholds.split(";");
	for(String input : inputValueswithThresholds){
		String[] val_thre = input.split(",");
		gValueTrees.put(val_thre[0], new Double(val_thre[1]));
	}
	return gValueTrees;
}

private static void valueFrameworkConditionCheck(Map<String, Double> gValueTrees) {	
//	thresholdRangeCheck(gValueTrees);
	if(queue!= null ) queue.clear();
	queue = new LinkedList<String>();
	initializeImportanceRange(gValueTrees);
	sortImportanceRange();
	initializeQueue();
	System.out.println("Input value threshold : ");
	printImportanceRange();
	thresholdRangeCheck_buffer();
	System.out.println("Final assigned threshold : ");
	printImportanceRange();
		
}

private static Map<String, Double> createRandomSet() {
	Map<String, Double> gValueTrees = new HashMap<String, Double>();
	Random rand = new Random();
	double upper = 100.0;
	double lower = 0.0;
	gValueTrees.put("UNIVERSALISM", (Math.random() * (upper - lower )) + lower);
	gValueTrees.put("POWER", (Math.random() * (upper - lower)) + lower);
	gValueTrees.put("SELFDIRECTION", (Math.random() * (upper - lower)) + lower);
	gValueTrees.put("TRADITION", (Math.random() * (upper - lower)) + lower);
	return gValueTrees;
}

private static void sortImportanceRange() {
	String item, itemTemp;
	String[] items;
	double biggestImportance = -1;
	double importance;
	for(int i =0; i < importanceRange.size(); i++){
		biggestImportance = new Double(importanceRange.get(i).split(";")[1]);
		for(int j = i+1; j < importanceRange.size(); j++ ){
			item = importanceRange.get(j);
			items = item.split(";");
			importance = new Double(items[1]);
			if(importance > biggestImportance){
				itemTemp = importanceRange.get(i);
				importanceRange.set(i, item);
				importanceRange.set(j, itemTemp);
				biggestImportance = importance;
			}
		}
	}
}


public static void thresholdRangeCheck_buffer() {	
	String valueTtl, qItem;
	double lowerBound, upperBound;
	while(queue.size()!=0){
		while (queue.size() != 0){
			qItem = getImportanceRange(queue.remove());
			valueTtl = getValueName(qItem);
			lowerBound = getLowerBound(qItem);
			upperBound = getUpperBound(qItem);
			if(lowerBound < 0 & upperBound < 0)continue;
			if(lowerBound < 0 | upperBound < 0)
				System.err.println("something is wroing bcs one the l and u bounds are negative " + lowerBound + ";" + upperBound);
			for(int irIdx = 0 ; irIdx < importanceRange.size(); irIdx++){
				String irItem = importanceRange.get(irIdx);
				double originalLowerBound = getLowerBound(irItem);
				double originalUpperBound = getUpperBound(irItem);
				if(valueTtl.equals(getValueName(irItem)) || (originalLowerBound >=0  & originalLowerBound == originalUpperBound)) continue;				
				//check for update
				//condition 1				
				String addToQueue1 = checkConditionOne(qItem, irItem);				
				//condition 2
				String addToQueue2 = checkConditiontwo(qItem, irItem);
				
				//intersection
				String intersectionSet = intersectionConditions(addToQueue1, addToQueue2);				
				double newLowerBound = getLowerBound(intersectionSet);
				double newUpperBound = getUpperBound(intersectionSet);
				
				if(newLowerBound != originalLowerBound  || newUpperBound != originalUpperBound ){
					queue.add(getValueName(irItem));
					importanceRange.set(irIdx, intersectionSet);
				}					
			}
		}
		String undetermindItem = findNextUndetermindItem(importanceRange);
		if(undetermindItem == null){
//			System.out.println("all values assigned");
			break;
		}
		else{
			double lowerThres =  getLowerBound(undetermindItem);
			double upperThres =  getUpperBound(undetermindItem);
			double randThreshold = (lowerThres+ (Math.random() * (upperThres - lowerThres)));
			String newItem = toString(getValueName(undetermindItem) , randThreshold, randThreshold, randThreshold);
			importanceRange.set(importanceRange.indexOf(undetermindItem), newItem);
			queue.add(getValueName(undetermindItem));
		}	
	}
	
	
}

private static void printImportanceRange() {
//	System.out.println("");
	for(String s: importanceRange){
		System.out.print(getValueName(s) + " " + getThreshold(s) + ";\t");
	}
	System.out.println("\n");
	
}

private static Double getThreshold(String item) {
	
	return new Double(item.split(";")[1]);
}

private static String getValueName(String item) {
	return item.split(";")[0];
}

private static String intersectionConditions(String addToQueue1,
		String addToQueue2) {
	double lb1 = getLowerBound(addToQueue1), lb2 = getLowerBound(addToQueue2), newLowerBound;
	String valueTitle = getValueName(addToQueue1);
	Double valueImportance = new Double(getThreshold(addToQueue1));
//	if(lb1 < 0 || lb2 < 0)
		newLowerBound = Math.max(lb1, lb2);
//	else
//		newLowerBound = Math.max(100, Math.max(lb1, lb2));
	double ub1 = getUpperBound(addToQueue1), ub2 = getUpperBound(addToQueue2), newUpperBound;
	if(ub1 < 0 || ub2 < 0)
		newUpperBound = Math.max(ub1, ub2);
	else
		newUpperBound = Math.min(ub1, ub2);
	newLowerBound = putInRange(newLowerBound);
	newUpperBound = putInRange(newUpperBound);
	String intersectionSet;
	intersectionSet = toString(valueTitle, valueImportance, newLowerBound, newUpperBound);
//	intersectionSet = toString(irItems[0], new Double(irItems[1]), newLowerBound, newUpperBound);				
	return intersectionSet;	
}

private static double getUpperBound(String intersectionSet) {
	return new Double(intersectionSet.split(";")[3]);
}

private static double getLowerBound(String intersectionSet) {
	return new Double(intersectionSet.split(";")[2]);
}

private static void initializeQueue() {
	double threhshold;
	boolean first = true;
	String valueTitle;
	for (String item : importanceRange) {
		threhshold = getThreshold(item);
		valueTitle = getValueName(item);
		if(first){
			queue.add(valueTitle);
			String addToQ = toString(valueTitle, threhshold, threhshold, threhshold) ;
			importanceRange.set(importanceRange.indexOf(toString(valueTitle, threhshold, -1, -1)), addToQ);
		}else
			queue.add(valueTitle);
		first = false;
	}
}

private static String getImportanceRange(String qItem) {
	for(String s:importanceRange){
		if(s.contains(qItem))
			return s;
	}
	return null;
}

private static String checkConditiontwo(String firstItem, String secondItem) {
	String firstItemName = getValueName(firstItem);
	String secondItemName = getValueName(secondItem);
	double secondItemThreshold = getThreshold(secondItem);
	int idx = AbstractValue.getIndexOfAbstractValue(firstItemName);
	if(idx == -1)
		return null;	
	int firstIdx = (idx <= (numOfAbstractValues/2+1)) ? (idx) : (numOfAbstractValues-idx);	
	idx = AbstractValue.getIndexOfAbstractValue(secondItemName);
	if(idx == -1)
		return null;		
	int secondIdx = (idx <= (numOfAbstractValues/2+1)) ? (idx) : (numOfAbstractValues-idx);
	
	double firstThresLower = new Double(getLowerBound(firstItem));
	double firstThresUpper = new Double(getUpperBound(firstItem));
	double secondThresLower =new Double(getLowerBound(secondItem));						
	double secondThresUpper= new Double(getUpperBound(secondItem));
	
	double maxOfSummation = 100 + multiplier/2;
	double minOfSummation = 100 - multiplier/2;
	
	if(secondIdx == (firstIdx + (numOfAbstractValues/2))%numOfAbstractValues){
		double lb, ub;

		lb = putInRange(Math.min(minOfSummation-firstThresLower,minOfSummation-firstThresUpper));		
		ub = putInRange(Math.max(maxOfSummation-firstThresUpper, maxOfSummation-firstThresLower));

		String intersection = intersectionConditions(toString(secondItemName, secondItemThreshold, secondThresLower, secondThresUpper), 
				toString(secondItemName, secondItemThreshold, Math.min(lb, ub), Math.max(lb, ub)));
		secondThresLower = getLowerBound(intersection);
//		secondThresLower = 0.0;
		secondThresUpper = getUpperBound(intersection);
	}	
	
	if(secondThresLower > secondThresUpper){ 
//		System.err.println("lowerThreshold is greater than upperThreshold" + secondThresLower + " > " + secondThresUpper);
		secondThresLower = 0;
		secondThresUpper = 100;
	}
	return toString(secondItemName, secondItemThreshold, secondThresLower, secondThresUpper);
}

private static double putInRange(double d) {
	if(d <0)
		return 0;
	if(d> 100)
		return 100;
	return d;
}

private static String checkConditionOne(String firstItem, String secondItem) {
	String[] firstItemPieces  = firstItem.split(";");
	String[] secondItemPieces = secondItem.split(";");
	
	int firstIdx = AbstractValue.getIndexOfAbstractValue(firstItemPieces[0]);
	if(firstIdx == -1)
		return null;	
	int secondIdx = AbstractValue.getIndexOfAbstractValue(secondItemPieces[0]);
	if(secondIdx == -1)
		return null;	
	int indexDifference = Math.abs((firstIdx-secondIdx));
	indexDifference = indexDifference <= (numOfAbstractValues/2)? indexDifference : numOfAbstractValues-indexDifference;

	double firstThresLower = Math.max(0,new Double(firstItemPieces[2]));
	double firstThresUpper = Math.max(0,new Double(firstItemPieces[3]));
	double secondThresLower = Math.max(0.0, firstThresLower - indexDifference * multiplier);						
	double secondThresUpper = Math.min(100.0, firstThresUpper  + indexDifference * multiplier);
	
	double lr = new Double(secondItemPieces[2]);
	double ur = new Double(secondItemPieces[3]);
	if(lr >= 0)
		secondThresLower = Math.max(Math.max(lr, secondThresLower), 0);
	if(ur >=0)
		secondThresUpper = Math.min(Math.min(ur, secondThresUpper), 100);
	
	if(secondThresLower > secondThresUpper)
		System.err.println("lowerThreshold is greater than upperThreshold" + secondThresLower + " > " + secondThresUpper);
	return toString(secondItemPieces[0], new Double(secondItemPieces[1]), secondThresLower, secondThresUpper);
}

private static void initializeImportanceRange(Map<String, Double> gValueTrees) {
	for(String valueName : gValueTrees.keySet()){
		importanceRange.add(toString(valueName, gValueTrees.get(valueName), -1, -1));
	}
	
}

private static String findNextUndetermindItem(ArrayList<String> importanceRange ) {
	for(String item : importanceRange){
		String[] items = item.split(";");
		if(!items[2].equals(items[3]))
			return item;
	}
	return null;
}

private static String toString(String valueTitle,
		double threhshold, double lowerBound, double upperBound) {	
	return valueTitle + ";" + threhshold + ";" + lowerBound + ";" + upperBound;
}

public static void thresholdRangeCheck(Map<String, Integer> gValueTrees) {

	double firstThreshold = -1, secondThreshold;
	double epsilon = 5;
	int firstIdx, secondIdx;
	String firstAbsName, secondAbsName;
	String[]range = new String[numOfAbstractValues]; //lowerRange + ";" + upperRange
	double upperRange, lowerRange;
	int loop = 20;
	while(loop!=0){
		loop--;
		for(int i = 0; i < numOfAbstractValues; i++){
		
			firstAbsName = AbstractValue.getAbstractValueByIndex(i).name();
			if(gValueTrees.containsKey(firstAbsName)){
				
				double firstThresLower = firstThreshold, firstThresUpper = firstThreshold;
				if(range[i] == null){
					firstThreshold = (int) gValueTrees.get(firstAbsName);
					range[i] = firstThreshold + ";" + firstThreshold;	
					firstThresUpper = firstThreshold;
					firstThresLower = firstThreshold;
				}
				else{
//					double firstThresLower, firstThresUpper;
					firstThresLower = new Double(range[i].split(";")[0]);					
					firstThresUpper = new Double(range[i].split(";")[1]);
					if(firstThresLower!= firstThresUpper){
						firstThreshold = (firstThresLower + (Math.random() * (firstThresUpper - firstThresLower)));
						range[i] = firstThreshold + ";" + firstThreshold;	
					}
					else{
						firstThreshold = firstThresLower;
						range[i] = firstThresLower + ";" + firstThresUpper;
					}
					
				}
				firstIdx = (i <= (numOfAbstractValues/2+1)) ? (i) : (numOfAbstractValues-i);
//				System.out.println("firstAbsName : " + firstAbsName + ",\tfirstThreshold : " + firstThreshold + ",\tfirstIdx : " + firstIdx);											
				for(int j = i+1; j < numOfAbstractValues; j++){
					secondAbsName = AbstractValue.getAbstractValueByIndex(j).name();
					if(gValueTrees.containsKey(secondAbsName)){						
//						secondThreshold = (int) gValueTrees.get(secondAbsName);
						secondIdx = (j <= (numOfAbstractValues/2+1)) ? (j) : (numOfAbstractValues-j);
//						System.out.println("secondAbsName : " + secondAbsName + ",\tsecondThreshold : " + secondThreshold + ",\tsecondIdx : " + secondIdx);

						//condition 1
						upperRange = Math.min(100.0, firstThresUpper + Math.abs((double)(firstIdx-secondIdx))/(double)numOfAbstractValues *100.0 + epsilon);
						lowerRange = Math.max(0.0, firstThresLower - Math.abs((double)(firstIdx-secondIdx))/(double)numOfAbstractValues *100.0 - epsilon);						
						if(range[j] !=null){
							double lr = new Double(range[j].split(";")[0]);
							double ur = new Double(range[j].split(";")[1]);
							if(lr!=ur) {
								if(lr> lowerRange)
									lowerRange = lr;
								if(ur < upperRange)
									upperRange = ur;
							}
							else 
								continue;
						}
						//condition 2
						if(upperRange!=lowerRange){
							if(secondIdx == (firstIdx + (numOfAbstractValues/2))%numOfAbstractValues){
								upperRange = Math.min(upperRange, 100-firstThreshold);
							}
						}
						
						if(upperRange < lowerRange)
							System.err.println("error occured!");
						range[j] = lowerRange + ";" + upperRange;						
					}
				}
			}
		}
		System.out.println("loop " + loop);
		System.out.println(range[0] + ", " + range[1] + ", " +range[2] + ", " +range[3] + ", " +range[4] + ", " +range[5] + ", " +range[6] + ", " +range[7] + ", " +range[8] + ", " +range[9] + ", ");
	}
}




}
