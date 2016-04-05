/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.solver;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.optaplanner.core.impl.solver.termination.Termination;

/**
 * A Solver solves a planning problem.
 * Clients usually call {@link #solve(Solution_)} and then {@link #getBestSolution()}.
 * <p>
 * These methods are not thread-safe and should be called from the same thread,
 * except for the methods that are explicitly marked as thread-safe.
 * Note that despite that {@link #solve(Solution_)} is not thread-safe for clients of this class,
 * that method is free to do multi-threading inside itself.
 * <p>
 * Build by a {@link SolverFactory}.
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public interface Solver<Solution_> {

    /**
     * The best solution is the {@link PlanningSolution best solution} found during solving:
     * it might or might not be optimal, feasible or even initialized.
     * <p>
     * The {@link #solve(Solution_)} method also returns the best solution,
     * but this method is useful in rare asynchronous situations (although
     * {@link SolverEventListener#bestSolutionChanged(BestSolutionChangedEvent)} is often more appropriate).
     * <p>
     * This method is thread-safe.
     * @return never null, but it can return the original, uninitialized {@link PlanningSolution} with a {@link Score} null.
     */
    Solution_ getBestSolution();

    /**
     * Returns the {@link Score} of the {@link #getBestSolution()}.
     * <p>
     * This is useful for generic code, which doesn't know the type of the {@link PlanningSolution}
     * to retrieve the {@link Score} from the {@link #getBestSolution()} easily.
     * @return null if the {@link PlanningSolution} is still uninitialized
     */
    Score getBestScore();

    /**
     * @return the amount of millis spent between when this solver started (or last restarted) and ended
     */
    long getTimeMillisSpent();

    /**
     * Solves the planning problem and returns the best solution encountered
     * (which might or might not be optimal, feasible or even initialized).
     * <p>
     * It can take seconds, minutes, even hours or days before this method returns,
     * depending on the {@link Termination} configuration.
     * To terminate a {@link Solver} early, call {@link #terminateEarly()}.
     * @param planningProblem never null, usually its planning variables are uninitialized
     * @return never null, but it can return the original, uninitialized {@link PlanningSolution} with a {@link Score} null.
     * @see #terminateEarly()
     */
    Solution_ solve(Solution_ planningProblem);

    /**
     * This method is thread-safe.
     * @return true if the {@link #solve(Solution_)} method is still running.
     */
    boolean isSolving();

    /**
     * Notifies the solver that it should stop at its earliest convenience.
     * This method returns immediately, but it takes an undetermined time
     * for the {@link #solve(Solution_)} to actually return.
     * <p>
     * This method is thread-safe.
     * @return true if successful
     * @see #isTerminateEarly()
     * @see Future#cancel(boolean)
     */
    boolean terminateEarly();

    /**
     * This method is thread-safe.
     * @return true if terminateEarly has been called since the {@link Solver} started.
     * @see Future#isCancelled()
     */
    boolean isTerminateEarly();

    /**
     * Schedules a {@link ProblemFactChange} to be processed.
     * <p>
     * As a side-effect, this restarts the {@link Solver}, effectively resetting all {@link Termination}s,
     * but not {@link #terminateEarly()}.
     * <p>
     * This method is thread-safe.
     * Follows specifications of {@link BlockingQueue#add(Object)} with by default
     * a capacity of {@link Integer#MAX_VALUE}.
     * @param problemFactChange never null
     * @return true (as specified by {@link Collection#add})
     */
    boolean addProblemFactChange(ProblemFactChange<Solution_> problemFactChange);

    /**
     * Checks if all scheduled {@link ProblemFactChange}s have been processed.
     * <p>
     * This method is thread-safe.
     * @return true if there are no {@link ProblemFactChange}s left to do
     */
    boolean isEveryProblemFactChangeProcessed();

    /**
     * @param eventListener never null
     */
    void addEventListener(SolverEventListener<Solution_> eventListener);

    /**
     * @param eventListener never null
     */
    void removeEventListener(SolverEventListener<Solution_> eventListener);

    /**
     * Useful to reuse the {@link Score} calculation in a UI (or even to explain the {@link Score} in a UI).
     *
     * @return never null
     */
    ScoreDirectorFactory<Solution_> getScoreDirectorFactory();

}
