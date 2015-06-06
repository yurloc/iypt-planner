package org.iypt.planner.gui;

import com.neovisionaries.i18n.CountryCode;
import java.util.Arrays;
import java.util.Collections;
import org.iypt.planner.domain.JurorLoad;
import org.iypt.planner.domain.SampleFacts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JurorDetailsTest {

    private JurorAssignment assignment;
    private JurorInfo info;
    private JurorDetails details;
    @Mock
    private RoundModel round;
    @Mock
    private JurorDetailsListener listener;

    @Before
    public void setUp() {
        Mockito.when(round.toString()).thenReturn("");
        Mockito.when(round.getNumber()).thenReturn(1);
        SampleFacts.gABC.setName("X");
        assignment = new JurorAssignment(round, SampleFacts.gABC);
        info = new JurorInfo(
                SampleFacts.jA1,
                Collections.<CountryCode>emptyList(),
                Arrays.asList(assignment),
                new JurorLoad(SampleFacts.jA1, 1, 5, 0, 1)
        );
        details = new JurorDetails();
    }

    @Test
    public void testShowJuror() {
        details.getDetailsListeners().add(listener);
        details.showJuror(info);
        verify(listener).jurorChanged();
        assertThat(details.getJurorInfo()).isEqualTo(info);
        assertThat(assignment.getOriginalStatus()).isEqualTo(JurorAssignment.Status.ASSIGNED);
        assertThat(assignment.getCurrentStatus()).isEqualTo(JurorAssignment.Status.ASSIGNED);
    }

    @Test
    public void testAssignmentChange() {
        details.showJuror(info);

        details.getDetailsListeners().add(listener);
        details.changeAssignment(assignment, JurorAssignment.Status.AWAY.ordinal());
        verify(listener, atLeastOnce()).jurorAssignmentChanged();
        assertThat(assignment.getOriginalStatus()).isEqualTo(JurorAssignment.Status.ASSIGNED);
        assertThat(assignment.getCurrentStatus()).isEqualTo(JurorAssignment.Status.AWAY);
    }

    @Test
    public void testRevertChange() {
        details.showJuror(info);

        // prepare a change
        details.changeAssignment(assignment, JurorAssignment.Status.AWAY.ordinal());

        // revert it
        details.getDetailsListeners().add(listener);
        details.revertSchedule();
        verify(listener).jurorChanged();
        verify(listener).jurorAssignmentChanged();
        assertThat(assignment.getOriginalStatus()).isEqualTo(JurorAssignment.Status.ASSIGNED);
        assertThat(assignment.getCurrentStatus()).isEqualTo(JurorAssignment.Status.ASSIGNED);
    }

    @Test
    public void testSaveChange() {
        details.showJuror(info);

        // make a change
        details.changeAssignment(assignment, JurorAssignment.Status.IDLE.ordinal());
        assertThat(assignment.getCurrentStatus()).isEqualTo(JurorAssignment.Status.IDLE);

        // save it
        details.getDetailsListeners().add(listener);
        details.saveChanges();
        verify(listener).jurorChangesSaved(details);
    }
}
