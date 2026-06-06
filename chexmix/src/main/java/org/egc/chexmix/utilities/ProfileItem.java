package org.egc.chexmix.utilities;

class ProfileItem {
	final Integer index;
	final Profile profile;

	ProfileItem(int idx, Profile p) { index = idx; profile = p; }
	ProfileItem(Profile p) { index = null; profile = p; }

	int dimension() { return profile.length(); }
	double getValue(int i) { return profile.value(i); }
}
