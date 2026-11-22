# Reproduce live GUI acceptance checks

Use the same Java 8 installation as the application on macOS. From the repository root, after setting `JAVA_HOME`:

```sh
mvn clean test
mkdir -p target/evidence-classes target/evidence-preferences
"$JAVA_HOME/bin/javac" -cp 'target/classes:SweetHome3D/lib/*:SweetHome3D/libtest/*' \
  -d target/evidence-classes tools/CaptureAcceptanceEvidence.java
"$JAVA_HOME/bin/java" \
  -cp 'target/evidence-classes:target/classes:SweetHome3D/lib/*:SweetHome3D/libtest/*' \
  com.eteks.sweethome3d.CaptureAcceptanceEvidence \
  "$PWD" "$PWD/target/evidence-preferences"
python3 test_submission.py
```

Run from a graphical desktop. The program launches the actual application, opens the supplied sample, edits the actual furniture dialog, exercises sorting and menus, generates the application PDF, and checks restoration and page boundaries. It replaces the acceptance files in `submissions/` with fresh captures. A successful run prints `GUI_ACCEPTANCE_PASS` and exits.

UI snapshots use `Window.printAll` on live windows; they exclude unrelated desktop content and native macOS title bars. The PDF comes from the production print-to-PDF implementation. The checks use temporary preferences and in-memory edits, leaving the sample file unchanged. This program is separate from the unmodified grading tests.
