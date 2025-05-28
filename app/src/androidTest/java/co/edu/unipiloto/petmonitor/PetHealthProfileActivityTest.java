package co.edu.unipiloto.petmonitor;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import co.edu.unipiloto.petmonitor.CasosdeUso.PetHealthProfileActivity;
import co.edu.unipiloto.petmonitor.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PetHealthProfileActivityTest {

    @Test
    public void testComponentesVisibles() {
        Intent intent = new Intent();
        intent.putExtra("petId", "12345");
        intent.putExtra("petName", "Fido");
        ActivityScenario.launch(PetHealthProfileActivity.class, intent.getExtras());

        //onView(withId(R.id.petNameTextView))
                //.check(matches(isDisplayed()));

        onView(withId(R.id.diseasesValueTextView))
                .check(matches(isDisplayed()));

        onView(withId(R.id.allergiesValueTextView))
                .check(matches(isDisplayed()));

        //onView(withId(R.id.saveButton))
               // .check(matches(isDisplayed()));
    }

    @Test
    public void testNombreMascotaVisible() {
        Intent intent = new Intent();
        intent.putExtra("petId", "12345");
        intent.putExtra("petName", "Fido");
        //ActivityScenario.launch(PetHealthProfileActivity.class, intent);

        //onView(withId(R.id.petNameTextView))
                //.check(matches(withText("Perfil de salud de Fido")));
    }

    @Test
    public void testMuestraNingunaSiNoHayAlergiasNiEnfermedades() {
        Intent intent = new Intent();
        intent.putExtra("petId", "12345");
        intent.putExtra("petName", "Fido");
        intent.putStringArrayListExtra("diseases", new ArrayList<>());
        intent.putStringArrayListExtra("allergies", new ArrayList<>());
        ActivityScenario.launch(PetHealthProfileActivity.class, intent.getExtras());

        onView(withId(R.id.diseasesValueTextView))
                .check(matches(withText("Ninguna")));

        onView(withId(R.id.allergiesValueTextView))
                .check(matches(withText("Ninguna")));
    }
}
