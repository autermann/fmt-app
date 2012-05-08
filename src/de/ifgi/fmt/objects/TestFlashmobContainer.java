package de.ifgi.fmt.objects;

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
		Point location1 = new Point(52.962625, 7.625556); // MŸnster, Domplatz
		Point location2 = new Point(52.956389, 7.634722); // MŸnster, Hauptbahnhof
		Point location3 = new Point(52.958472, 7.611111); // MŸnster, Zentralfriedhof

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
