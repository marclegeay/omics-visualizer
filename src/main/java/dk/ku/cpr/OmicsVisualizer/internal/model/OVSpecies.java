package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class OVSpecies implements Comparable<OVSpecies> {
	private Integer taxonID;
	private String abbreviatedName;
	private String scientificName;
	private static Map<String, OVSpecies> nameSpecies;
	private static List<OVSpecies> modelSpecies;
	private static List<OVSpecies> allSpecies;
	private static OVSpecies humanSpecies;
	
	public OVSpecies(Map<String,String> data) {
		super();
		this.taxonID = Integer.valueOf(data.get("taxonomyId"));
		this.abbreviatedName = data.get("scientificName"); // TODO: Bug ? The scientificName is the shortest one
		this.scientificName =  data.get("abbreviatedName"); // TODO: Bug ? The abbreviatedName looks like a scientific Name
	}

	public int compareTo(OVSpecies t) {
		if (t.toString() == null) return 1;
		return this.toString().compareTo(t.toString());
	}

	public static List<OVSpecies> search(String str) {
		List<OVSpecies> retValue = new ArrayList<OVSpecies>();
		for (String s: nameSpecies.keySet()) {
			if (s.regionMatches(true, 0, str, 0, str.length())) { 
				retValue.add(nameSpecies.get(s));
			}
		}
		return retValue;
	}

	public static List<OVSpecies> readSpecies(List<Map<String, String>> speciesFromTask) throws Exception {
		modelSpecies = new ArrayList<OVSpecies>();
		allSpecies = new ArrayList<OVSpecies>();
		nameSpecies = new TreeMap<String, OVSpecies>();

		for (Map<String, String> r : speciesFromTask) {
			OVSpecies species = new OVSpecies(r);
			allSpecies.add(species);
			nameSpecies.put(species.toString(), species);

			if (species.getAbbrevName().equals("Homo sapiens")) {
				modelSpecies.add(species);
				humanSpecies = species;
			}
		}

		// sort all collections
		Collections.sort(allSpecies);
		Collections.sort(modelSpecies);

		return allSpecies;
	}

	public static OVSpecies getHumanSpecies() {
		return humanSpecies;
	}

	public static List<OVSpecies> getAllSpecies() {
		return allSpecies;
	}

	public static List<OVSpecies> getModelSpecies() {
		return modelSpecies;
	}

	public Integer getTaxonID() {
		return this.taxonID;
	}
	
	public String getName() {
		return this.scientificName; // TODO: it should return the "abbreviatedName" from stringApp
	}

	public String getAbbrevName() {
		return this.abbreviatedName; // TODO: it should return the "abbreviatedName" from stringApp
	}

	public String getQueryString() {
		return (this.abbreviatedName + " " + this.scientificName).toLowerCase();
	}

	public String toString() {
		return this.scientificName;
	}
}
