# Generative AI disclosure

Date: November 22, 2026

## Tool and scope

ChatGPT/Codex was used extensively to complete this assignment at the student's request. The AI assistant read the CSC207 Quercus and MarkUs assignment information, the course syllabus, the repository instructions, public acceptance tests, reference images and sample PDF, and relevant application source code.

The AI assistant authored the submitted feature implementation, the GUI verification program, this disclosure, and the completion report. It also set up a local Java 8 runtime, ran the build and tests, generated acceptance evidence from the running application, reviewed the evidence, and managed the feature branches, commits, and GitHub Issues. This was substantial implementation assistance, not merely spelling correction or a conceptual explanation.

The initiating request was to explain the CSC207 mock internship assignment, complete it in full using its resources, and disclose AI use. The student subsequently authorized Java UI automation to verify features and capture evidence when ordinary desktop controls could not attach reliably to the Java application.

## Contributions by task

| Task | AI contribution |
| --- | --- |
| 1 | Read setup instructions, obtained Java 8, built and launched the application, and captured the running app's UI. |
| 2 | Changed description editability and its documentation, and verified that editing the live dialog updates the furniture object. |
| 3 | Implemented calculated Volume, comparator, default column, table renderer/export, menu actions and English resources; checked calculation and ascending/descending sorting. |
| 4 | Implemented all-viewable-level plan pages and an all-level furniture table with a print-only Level column; checked PDF output and restoration of state. |
| Workflow | Created the required issues, feature branches and merges, linked commits and evidence, and prepared submission information. |

## Verification and evidence provenance

The supplied `assignment-tests` were not edited. They were run against the implementation. Additional live GUI checks are reproducible with `tools/CaptureAcceptanceEvidence.java`.

The PNG files are application-only snapshots rendered directly from live Java windows with `Window.printAll`, including the actual editable dialog and menus. This avoids capturing unrelated desktop contents when macOS screen-capture permission is unavailable. They are not AI-generated illustrations. Native macOS title bars and the global screen menu are outside these Java window renderings. The PDF was produced by the application's real `HomePane.printToPDF` path using the supplied `MultiLevelHome.sh3d` sample. The UI checks operate on a temporary in-memory home and do not modify the supplied sample file.

## Student responsibility

The AI has not verified that the student has independently reviewed, understood, or can explain the submitted code. No such certification is made here. The course and assignment place responsibility on the student to understand and test any submitted work. The student should review the changes and this disclosure before making the final MarkUs submission.

The instructions inspected permit generative AI and do not prescribe a particular disclosure template. This file records the actual assistance explicitly, as requested by the student.
