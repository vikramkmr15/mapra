package geneticnetwork;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedList;

import org.junit.Test;

import geneticnetwork.algorithm.DataTransformerGeneticNetwork;
import geneticnetwork.algorithm.MatrixVectorBuilder;
import geneticnetwork.algorithm.NetworkScoreAlgorithm;
import geneticnetwork.algorithm.RandomWalkWithRestart;
import geneticnetwork.io.FileUtilitiesGeneticNetwork;
import io.FileInputReader;
import phenotogeno.algo.ScoredGene;

//TODO: run tests with driver!
public class TestGeneticNetworkScore {
	
	private String [][] networkFromFile;
	private HashMap<String, Double> scoresFromFile;
	
	private double [] scoresExp;
	private String [] genesExp;

	@Test
	public void testCase1WithoutDriver() {
		
		prepareDataForCase(1);
		NetworkScoreAlgorithm n1= new NetworkScoreAlgorithm(getBuilder(), new RandomWalkWithRestart(3, 0.9));
		LinkedList<ScoredGene> res1 = n1.runNetworkScoreAlgorithm();
		compareToExpected(res1);
	}
	
	@Test
	public void testCase2WithoutDriver() {
		prepareDataForCase(2);
		NetworkScoreAlgorithm n2= new NetworkScoreAlgorithm(getBuilder(), new RandomWalkWithRestart(1, 0.5));
		LinkedList<ScoredGene> res2 = n2.runNetworkScoreAlgorithm();
		compareToExpected(res2);
	}
	
	@Test
	public void testCase3WithoutDriver() {
		prepareDataForCase(3);
		NetworkScoreAlgorithm n3= new NetworkScoreAlgorithm(getBuilder(), new RandomWalkWithRestart(2, 0.2));
		LinkedList<ScoredGene> res3 = n3.runNetworkScoreAlgorithm();
		compareToExpected(res3);
	}
	
	private void prepareDataForCase(int testCase){
		
		//read network edges
		boolean weight=false;
		if(testCase==2|| testCase==3){
			weight = true;
		}
		networkFromFile = FileUtilitiesGeneticNetwork.readEdges("../TestData/GeneticNetwork/MTGNetwork.txt", weight);
		
		//read scores
		String fileScores="";
		if(testCase==1|| testCase==2){
			fileScores="../TestData/GeneticNetwork/Input_PTG1.txt";
		}
		else if(testCase==3){
			fileScores="../TestData/GeneticNetwork/Input_PTG2.txt";
		}
		scoresFromFile = FileUtilitiesGeneticNetwork.readGeneScoresFrom(fileScores);
		
		//read expected results
		LinkedList<String> exp = FileInputReader.readAllLinesFrom(
				"../TestData/GeneticNetwork/ExpectedRes/res"+testCase+".txt");
		scoresExp = new double[exp.size()];
		genesExp = new String[exp.size()];
		int pos=0;
		for(String line:exp){
			String [] split = line.split("\t");
			scoresExp[pos]=Double.parseDouble(split[1]);
			genesExp[pos] = split[0];
			pos++;
		}
		
	}
	
	private MatrixVectorBuilder getBuilder(){
		DataTransformerGeneticNetwork dt = new DataTransformerGeneticNetwork();
		return new MatrixVectorBuilder(dt.transformEdges(networkFromFile), dt.transformGeneScores(scoresFromFile));
	}
	
	private void compareToExpected(LinkedList<ScoredGene> g){
		int pos=0;
		assertEquals("Result size is incorrect", genesExp.length, g.size());
		for(ScoredGene gene:g){
			assertEquals("Gene id at position"+pos+" is incorrect", genesExp[pos], gene.getId());
			assertEquals("Score at position"+pos+" is incorrect", scoresExp[pos], gene.getScore(),1E-9);
			pos++;
		}
	}

}
