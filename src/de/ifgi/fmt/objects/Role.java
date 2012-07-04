package de.ifgi.fmt.objects;

public class Role {
	private String id;
	private String title;
	private String description;
	private int minParticipants;
	private int maxParticipants;
	private String[] items;

	public String returnItemsAsString() {
		if (items.length > 0) {
			String itemsAsString = "";
			for (String item : items) {
				itemsAsString = itemsAsString + "- " + item + "\n";
			}
			return itemsAsString;
		} else {
			return "None.";
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getMinParticipants() {
		return minParticipants;
	}

	public void setMinParticipants(int minParticipants) {
		this.minParticipants = minParticipants;
	}

	public int getMaxParticipants() {
		return maxParticipants;
	}

	public void setMaxParticipants(int maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	public String[] getItems() {
		return items;
	}

	public void setItems(String[] items) {
		this.items = items;
	}
}
