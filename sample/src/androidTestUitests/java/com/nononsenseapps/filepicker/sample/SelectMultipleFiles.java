package com.nononsenseapps.filepicker.sample;


import android.net.Uri;
import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.nononsenseapps.filepicker.Utils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.nononsenseapps.filepicker.sample.PermissionGranter.allowPermissionsIfNeeded;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SelectMultipleFiles {

    @Rule
    public ActivityTestRule<NoNonsenseFilePickerTest> mActivityTestRule =
            new ActivityTestRule<>(NoNonsenseFilePickerTest.class);

    @Before
    public void allowPermissions() {
        allowPermissionsIfNeeded(mActivityTestRule.getActivity());
    }

    /**
     * Should not throw on Android 24
     */
    @Test
    public void selectTwoFilesDoesNotThrowWithClipData() {
        NoNonsenseFilePicker.useClipData = true;

        selectTwoFilesDoesNotThrow();
    }

    /**
     * Should not throw on Android 24
     */
    @Test
    public void selectTwoFilesDoesNotThrowWithStringExtras() {
        NoNonsenseFilePicker.useClipData = false;

        selectTwoFilesDoesNotThrow();
    }

    private void selectTwoFilesDoesNotThrow() {
        ViewInteraction radioButton = onView(
                allOf(withId(R.id.radioFile), withText("Select file"),
                        withParent(withId(R.id.radioGroup)),
                        isDisplayed()));
        radioButton.perform(click());

        ViewInteraction checkBox = onView(
                allOf(withId(R.id.checkAllowMultiple), withText("Multiple items"), isDisplayed()));
        checkBox.perform(click());

        ViewInteraction button = onView(
                allOf(withId(R.id.button_sd), withText("Pick SD-card"), isDisplayed()));
        button.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(android.R.id.list), isDisplayed()));

        // Refresh view (into dir, and out again)
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        // Click on test dir
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        // sub dir
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        // Select first two
        recyclerView.perform(actionOnItemAtPosition(1, click()));
        recyclerView.perform(actionOnItemAtPosition(2, click()));

        // Click OK
        ViewInteraction okButton = onView(
                allOf(withId(R.id.nnf_button_ok),
                        withParent(allOf(withId(R.id.nnf_button_container),
                                withParent(withId(R.id.nnf_buttons_container)))),
                        isDisplayed()));
        okButton.perform(click());

        ViewInteraction textView = onView(withId(R.id.text));
        textView.check(matches(
                withText("/storage/emulated/0/000000_nonsense-tests/A-dir/file-1.txt\n" +
                        "/storage/emulated/0/000000_nonsense-tests/A-dir/file-0.txt")));

        // Make sure we can read one
        File file =
                Utils.getFileForUri(
                        Uri.parse("content://com.nononsenseapps.filepicker.sample.provider/root/storage/emulated/0/000000_nonsense-tests/A-dir/file-1.txt"));
        assertEquals("/storage/emulated/0/000000_nonsense-tests/A-dir/file-1.txt", file.toString());
        assertTrue(file.isFile());
        assertTrue(file.canRead());
        assertTrue(file.canWrite());
    }
}
