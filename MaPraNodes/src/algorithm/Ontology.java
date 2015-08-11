package algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

//save ontology as data structure
public class Ontology {
	//in ontology ist zu jedem knoten die liste seiner eltern gespeichert
	// wurzel hat leere liste
	private HashMap<Integer,LinkedList<Integer>> ontology;
	
	
	// konstruktor bekommt ein 2d array, das alle kanten aus isa enth�lt (child_id, parent_id)
	public Ontology(int[][] edges){
		ontology = new HashMap<Integer,LinkedList<Integer>>();
		for(int position=0; position<edges.length; position++){
			addEdge(edges[position][1],edges[position][0]);
		}
	}

	
	//methode, die eine kante zwischen parent und child hinzuf�gt (falls sie noch nicht existiert)
	private void addEdge(int parent, int child){
		//falls child-knoten schon enthalten, elter evtl in parentlist hinzuf�gen
		if(ontology.containsKey(child)){
			if(!ontology.get(child).contains(parent)){
				ontology.get(child).add(parent);
			}	
		}
		//falls child-knoten noch nicht enthalten, neu hinzuf�gen
		else{
			LinkedList<Integer> parentList = new LinkedList<Integer>();
			parentList.add(parent);
			ontology.put(child, parentList);
		}
		//falls elter nicht enthalten (zb bei wurzel) -> hinzuf�gen mit leerer liste
		if(!ontology.containsKey(parent)){
			LinkedList<Integer> parentList = new LinkedList<Integer>();
			ontology.put(parent, parentList);
		}
	}
	

	// gibt eine Menge (HashSet) aller vorfahren eines knotens zur�ck (selbst auch enthalten!)
	//wurzel gibt liste mit sich selbst zur�ck; in ontologie nicht vorhandener knoten ebenfalls
	public HashSet<Integer> getAllAncestors (int node){
		HashSet<Integer> ancestors = new HashSet<Integer>();
		if(ontology.containsKey(node)){
			addAncestors(node, ancestors);
			return ancestors;
		}
		else{
			ancestors.add(node);
			return ancestors;
		}
	}
	
	
	// n�tig f�r getAllAncestors()
	private void addAncestors(int actNode, HashSet<Integer> ancestors){
		if(ontology.containsKey(actNode)){
			if(!ancestors.contains(actNode)){
				//actNode noch nicht enthalten, also hinzuf�gen und rekursiv f�r alle parents von actNode aufrufen
				ancestors.add(actNode);
				LinkedList<Integer> parents = ontology.get(actNode);
				Iterator<Integer> iter = parents.iterator();
				while(iter.hasNext()){
					addAncestors(iter.next(), ancestors);
				}
			}
		}
	}
	
	
	//gibt alle common ancestors von 2 knoten zur�ck (falls es keinen gibt -> leere menge)
	public HashSet<Integer> getAllCommonAncestors (int node1, int node2){
		HashSet<Integer> commonAncestors = new HashSet<Integer>();
		HashSet<Integer> ancestors1 = getAllAncestors(node1);
		HashSet<Integer> ancestors2 = getAllAncestors(node2);
		
		//gemeinsamkeiten von ancestors1 und ancestors2 finden:
		// fall 1: node1 und/oder node2 sind nicht in ontologie (ancestors ist null) -> leeres Set zur�ckgeben
		if(ancestors1==null || ancestors2==null){
			return commonAncestors;
		}
		
		//fall 2: es gibt einen CA bzw mehrere CAs -> Schnitt beider mengen zur�ckgeben
		Iterator<Integer> iter1 = ancestors1.iterator();
		while(iter1.hasNext()){
			int actNode = iter1.next();
			if(ancestors2.contains(actNode) && !commonAncestors.contains(actNode)){
				commonAncestors.add(actNode);
			}
		}
		return commonAncestors;
	}
	
	
	// alternative zu getAllCommonAncestors:
	// gibt alle f�r IC relevanten ancestors von 2 knoten zur�ck (falls es keinen gibt -> leere menge)
	public HashSet<Integer> getRelevantCommonAncestors (int node1, int node2){
		HashSet<Integer> relevantAncestors = new HashSet<Integer>();
		HashSet<Integer> ancestors1 = getAllAncestors(node1);
		
		// f�r node2 nicht alle ancestors n�tig... 
		// wenn ein CA gefunden: f�r diesen pfad abbrechen (eltern von diesem CA irrelevant)
		addRelevantCommonAncestors(node2, relevantAncestors, ancestors1);
		return relevantAncestors;
	}
	
	// n�tig f�r getRelevantCommonAncestors()
	private void addRelevantCommonAncestors(int actNode, HashSet<Integer> relevantAncestors, HashSet<Integer> ancestors1){
		if(ancestors1.contains(actNode)){
			// ein CA gefunden, also zu relevantAncestors hinzuf�gen und eltern von actNode nicht mehr betrachten
			relevantAncestors.add(actNode);
		}
		else{
			// kein CA gefunden, also methode f�r eltern von actNode aufrufen
			if(ontology.containsKey(actNode)){
				LinkedList<Integer> parents = ontology.get(actNode);
				Iterator<Integer> iter = parents.iterator();
				while(iter.hasNext()){
					addRelevantCommonAncestors(iter.next(), relevantAncestors, ancestors1);
				}
			}
		}
		
	}
	
	
	
	
}






