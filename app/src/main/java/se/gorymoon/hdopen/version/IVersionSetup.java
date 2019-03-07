package se.gorymoon.hdopen.version;

import androidx.fragment.app.Fragment;

public interface IVersionSetup {

    Fragment getSlideFragment();

    void handleVersion(Fragment fragment);

}
