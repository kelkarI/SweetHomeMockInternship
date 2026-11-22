# CSC207 mock internship: requirements and completed work

## What the assignment requires

This is an individual maintenance assignment on Sweet Home 3D 5.4, worth 4% of the course grade. Fork the starter repository into your own GitHub account and work in that fork using feature branches, progress commits, and merges. Do not modify the supplied `assignment-tests`.

| Task | Required result | Included evidence |
| --- | --- | --- |
| 1 | Build, smoke-test, and launch the existing application. On macOS, use Java 8 and Maven 3.6.3 or newer. | `submissions/task1-running.png` |
| 2 | Make Description editable in Modify furniture, save edits to the furniture object, and correct nearby documentation. | `submissions/task2-description.png` |
| 3 | Compute width × depth × height for catalog and home furniture. Show Volume by default in new homes; support ascending/descending sorting; offer sort/display actions in Furniture menus and table context menus; provide English labels and tooltips. Use the existing length-number formatter as requested. | `submissions/task3-volume-values.png`, `submissions/task3-volume.png` |
| 4 | Print the furniture on every viewable level with a Level column, then one fitted plan page for each viewable level in home order. Restore the previous selected level even on a rendering failure, and preserve on-screen columns. | `submissions/task4-all-levels.pdf` |

Create one GitHub Issue for each of Tasks 2-4 using the supplied template. Link its implementation commits and acceptance evidence, then close it once verified. Commit evidence under `submissions/`, push all work to the fork, and leave the working tree clean. The submission checker accepts PNG/JPG/JPEG for Tasks 1-3 and those formats or PDF for Task 4; filenames must contain the corresponding task number.

## Submission and deadline

- Fork: https://github.com/kelkarI/SweetHomeMockInternship
- MarkUs assignment: https://markus.teach.cs.toronto.edu/markus/courses/176/assignments/1599
- Required MarkUs file: `github.com.markusurl`, containing the fork URL. MarkUs clones that repository when self-tests or grading run; submit the URL once.
- Recommended deadline shown on MarkUs: October 25, 2026, 11:59 p.m. Toronto time (extended from October 23).
- Final deadline shown on both Quercus and MarkUs: **November 20, 2026, 6 p.m. EST**.

The required `github.com.markusurl` file was submitted to MarkUs on November 22, 2026, at 4:39:01 p.m. EST. MarkUs confirmed that all required files were submitted, and the displayed file contents point to the correct fork. The server explicitly warns that changes after the deadline are recorded but not graded. No instructor-approved exception has been verified. Two MarkUs self-test runs completed after submission. Both show 3/3 for GitHub Issues, 8/8 for file-existence/evidence checks, and 6/6 for Maven tests (17/17 available marks). A separate zero-point `test_repo_download` diagnostic remains flagged as failed. No detailed error was exposed in the result UI. An independent fresh public clone of the submitted fork succeeded at the current commit; the cause of the server diagnostic has not been established.

## How the implementation works

1. `HomeFurnitureController.isPropertyEditable` now permits DESCRIPTION. The controller's existing binding, modification and undo machinery persists the value.
2. `getVolume()` uses current dimensions, so resizing cannot leave a stale cached volume. The VOLUME sortable property and comparator connect to the existing sorting and descending-order infrastructure. The same shared menu builders supply both main and context-menu controls. New English resource keys provide labels and descriptions. Volume also exports with the existing table number formatting.
3. `PlanComponent.print` maps a page index to the corresponding viewable level, fits one plan onto that page, and restores selection in a `finally` block. Negative and out-of-range requests return NO_SUCH_PAGE. Empty viewable levels retain their page.
4. `FurnitureTable.print` adds Level to a separate printable column model, prints the model's furniture from all viewable levels, and restores the original filter, column model and grid color in `finally`. `HomePrintableComponent` no longer restricts the furniture section to the selected level.

## Verification

- Starter `mvn -DskipTests package`: passed.
- Starter `mvn -Dtest=BuildSmokeTest test`: passed.
- Final `mvn clean test`: passed, all six provided tests.
- `python3 test_submission.py`: passed.
- MarkUs self-tests: 17/17 scored marks on two runs; a zero-point checkout diagnostic remains flagged as described above.
- Live GUI checks: catalog and resized furniture volumes; enabled menu controls and tooltip resources; working display toggles; restrictive furniture-filter restoration; table column/filter/grid restoration after an injected print failure; description editing and persistence; Volume column; ascending and descending row order; main/context sort and display entries; four-page sample PDF; selected-level and screen-column restoration; hidden-level page mapping; negative/out-of-range pages; selected-level restoration after rendering failure.
- Visual review: the PNGs show the running application and actual controls; the PDF shows all three levels in the furniture table and three distinct plan pages. The sample and provided tests remain unchanged.

See `tools/README.md` to reproduce the GUI checks. The assignment requires you to understand and be able to explain the submitted code.
