package com.own.virtualaibox.controller;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.own.virtualaibox.brain.LLMBrain;
import com.own.virtualaibox.core.TickSchedule;
import com.own.virtualaibox.core.VirtualClock;
import com.own.virtualaibox.core.WorldEngine;
import com.own.virtualaibox.domain.action.MoveAction;
import com.own.virtualaibox.domain.agent.Agent;
import com.own.virtualaibox.domain.event.EventBus;
import com.own.virtualaibox.domain.memory.InMemoryMemoryStore;
import com.own.virtualaibox.domain.world.WorldState;
import com.own.virtualaibox.mind.MindController;
import com.own.virtualaibox.monitor.ConvergenceMonitor;

class DashboardControllerTest {

    private DashboardController controller;

    @BeforeEach
    void setUp() {
        EventBus eventBus = new EventBus();
        VirtualClock virtualClock = mock(VirtualClock.class);
        when(virtualClock.getTick()).thenReturn(0);
        WorldEngine worldEngine = new WorldEngine(virtualClock, null, eventBus, new InMemoryMemoryStore());
        worldEngine.init();
        MindController mindController = new MindController(new FakeBrain());
        controller = new DashboardController(
                worldEngine,
                mindController,
                new ConvergenceMonitor(eventBus, mindController));
    }

    @Test
    void dashboardReturnsConsistentWorldAndResidentShapes() {
        Map<String, Object> dashboard = controller.dashboard(30, 8);

        Map<?, ?> world = (Map<?, ?>) dashboard.get("world");
        List<?> agents = (List<?>) dashboard.get("agents");
        assertEquals(2, world.get("agentCount"));
        assertEquals(2, agents.size());
        assertEquals(2, ((Map<?, ?>) dashboard.get("metrics")).get("agentCount"));

        Map<?, ?> firstAgent = (Map<?, ?>) agents.get(0);
        assertNotNull(firstAgent.get("id"));
        assertNotNull(firstAgent.get("name"));
        assertNotNull(firstAgent.get("memoryStats"));
        assertNotNull(firstAgent.get("recentMemories"));
    }

    @Test
    void addAgentReturnsCreatedResidentAndUpdatedWorld() {
        Map<String, Object> response = controller.addAgent(new AgentCreateRequest("Clara", 4, 9));

        Map<?, ?> created = (Map<?, ?>) response.get("agent");
        Map<?, ?> world = (Map<?, ?>) response.get("world");
        List<?> agents = (List<?>) response.get("agents");
        assertEquals("Clara", created.get("name"));
        assertEquals(4, created.get("x"));
        assertEquals(9, created.get("y"));
        assertEquals(3, world.get("agentCount"));
        assertEquals(3, agents.size());
    }

    @Test
    void stepReturnsWorldAndFullResidentShape() {
        TickSchedule tickSchedule = mock(TickSchedule.class);
        VirtualClock virtualClock = mock(VirtualClock.class);
        when(virtualClock.getTick()).thenReturn(1);
        EventBus eventBus = new EventBus();
        WorldEngine worldEngine = new WorldEngine(virtualClock, tickSchedule, eventBus, new InMemoryMemoryStore());
        worldEngine.init();
        MindController mindController = new MindController(new FakeBrain());
        DashboardController stepController = new DashboardController(
                worldEngine,
                mindController,
                new ConvergenceMonitor(eventBus, mindController));

        Map<String, Object> response = stepController.step();

        assertEquals(1, response.get("tick"));
        Map<?, ?> world = (Map<?, ?>) response.get("world");
        List<?> agents = (List<?>) response.get("agents");
        assertEquals(2, world.get("agentCount"));
        assertEquals(2, agents.size());
        assertNotNull(((Map<?, ?>) agents.get(0)).get("memoryStats"));
    }

    @Test
    void invalidAgentRequestIsMappedToBadRequestPayload() {
        Map<String, Object> response = controller.handleInvalidRequest(
                new IllegalArgumentException("agent name already exists"));

        assertEquals("agent name already exists", response.get("error"));
    }

    @Test
    void nullCreateRequestUsesWorldDefaults() {
        Map<String, Object> response = controller.addAgent(null);

        Map<?, ?> created = (Map<?, ?>) response.get("agent");
        Map<?, ?> world = (Map<?, ?>) response.get("world");
        assertNotNull(created.get("name"));
        assertEquals(3, world.get("agentCount"));
    }

    private static class FakeBrain extends LLMBrain {
        FakeBrain() {
            super(null);
        }

        @Override
        public MoveAction decideAction(Agent agent, WorldState worldState) {
            MoveAction action = new MoveAction();
            action.setAgentId(agent.getId());
            action.setDeltaX(1);
            action.setDeltaY(0);
            action.setReason("test");
            return action;
        }
    }
}
