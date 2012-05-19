package de.ifgi.fmt.objects;

import com.google.android.maps.GeoPoint;

public class TestFlashmobContainer
{
	// Fake set of flashmobs
	public Flashmob[] flashmobs = new Flashmob[3];
	public String[] flashmobTitles = new String[flashmobs.length];

	public TestFlashmobContainer()
	{
		createFakeFlashmobData();
	}

	/**
	 * Create fake flashmob data for testing.
	 */
	public void createFakeFlashmobData()
	{
		// Creating locations for the flashmobs
		double lat1 = 51.962625;
		double lon1 = 7.625556;
		double lat2 = 51.956389;
		double lon2 = 7.634722;
		double lat3 = 51.958472;
		double lon3 = 7.611111;

		GeoPoint location1 = new GeoPoint((int) (lat1 * 1E6), (int) (lon1 * 1E6)); // MŸnster,
																					// Domplatz
		GeoPoint location2 = new GeoPoint((int) (lat2 * 1E6), (int) (lon2 * 1E6)); // MŸnster,
																					// Hauptbahnhof
		GeoPoint location3 = new GeoPoint((int) (lat3 * 1E6), (int) (lon3 * 1E6)); // MŸnster,
																					// Zentralfriedhof

		// Creating pseudo-flashmobs
		Flashmob danceFlashmob = new Flashmob("001", "Dance Flashmob", location1, true, 66,
				"People dance in public to a certain song.");
		Flashmob freezeFlashmob = new Flashmob("002", "Freeze Flashmob", location2, true, 1001,
				"People freeze for 2 minutes.");
		Flashmob zombieFlashmob = new Flashmob("003", "Zombie Flashmob", location3, false, 42,
				"People dressed as zombies act on command.");

		// Adding the flashmobs to the array
		flashmobs[0] = danceFlashmob;
		flashmobs[1] = freezeFlashmob;
		flashmobs[2] = zombieFlashmob;

		// Adding the flashmobs titles to the array for displaying on the list view
		for (int i = 0; i < flashmobs.length; i++)
		{
			flashmobTitles[i] = flashmobs[i].getTitle();
		}
	}
}
