package de.ifgi.fmt.data;

import java.util.ArrayList;

import de.ifgi.fmt.objects.Flashmob;

import android.app.Application;

public class Store extends Application {
	private ArrayList<Flashmob> flashmobs;

	public ArrayList<Flashmob> getFlashmobs() {
		return flashmobs;
	}

	public void setFlashmobs(ArrayList<Flashmob> flashmobs) {
		this.flashmobs = flashmobs;
	}

	public Flashmob getFlashmobById(String id) {
		for (int i = 0; i < flashmobs.size(); i++) {
			if (flashmobs.get(i).getId().equals(id)) {
				return flashmobs.get(i);
			}
		}
		return null;
	}
}