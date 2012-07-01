package de.ifgi.fmt.data;

import java.util.ArrayList;

import de.ifgi.fmt.objects.Flashmob;

import android.app.Application;

public class Store extends Application {
	private ArrayList<Flashmob> flashmobs = new ArrayList<Flashmob>();

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

	public boolean hasFlashmob(Flashmob flashmob) {
		for (int i = 0; i < flashmobs.size(); i++) {
			if (flashmobs.get(i).getId().equals(flashmob.getId())) {
				return true;
			}
		}
		return false;
	}

	public void replaceFlashmob(Flashmob flashmob) {
		for (int i = 0; i < flashmobs.size(); i++) {
			if (flashmobs.get(i).getId().equals(flashmob.getId())) {
				flashmobs.remove(i);
				flashmobs.add(i, flashmob);
				return;
			}
		}
		flashmobs.add(flashmob);
	}

	public void addFlashmob(Flashmob flashmob) {
		this.flashmobs.add(flashmob);
	}
}