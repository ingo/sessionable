import XCTest

/// Apple's automated accessibility audit (contrast, clipped or truncated text, tap targets, missing labels,
/// Dynamic Type) run on the main screens, at the normal text size and at an accessibility size. Needs the network:
/// it connects to a real Sessionize conference. Findings are logged and attached as text, with a screenshot of each
/// screen, then judged by a person: some are real, some are false alarms. Not part of CI.
@MainActor
final class AccessibilityAuditTests: XCTestCase {
    private let app = XCUIApplication()
    private let conferenceId = "asqam7rn"
    private var findings: [String] = []

    override func setUp() {
        continueAfterFailure = true
    }

    private func launch(largeText: Bool, dark: Bool = false) {
        app.launchArguments = ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        if largeText {
            app.launchArguments += ["-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityXL"]
        }
        if dark { app.launchArguments += ["-AppleInterfaceStyle", "Dark"] }
        app.launch()
    }

    private func audit(_ screen: String) {
        let shot = XCTAttachment(screenshot: app.screenshot())
        shot.name = screen
        shot.lifetime = .keepAlways
        add(shot)
        do {
            try app.performAccessibilityAudit { issue in
                let element = issue.element.map {
                    "\($0.elementType.rawValue) '\($0.label)' frame=\($0.frame.integral)"
                } ?? "?"
                self.findings.append("\(screen): \(issue.compactDescription) - \(element)")
                return true // keep going and collect them all
            }
        } catch {
            findings.append("\(screen): audit failed to run: \(error)")
        }
    }

    private func report(_ name: String) {
        let text = findings.isEmpty ? "No findings" : findings.joined(separator: "\n")
        print("AUDIT[\(name)] \(findings.count) findings\n\(text)")
        let attachment = XCTAttachment(string: text)
        attachment.name = "findings-\(name)"
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    /// Connects to the conference by typing its id, as an attendee would.
    private func connect() {
        // Compose text fields are exposed as text views
        let fields = app.textViews
        XCTAssertTrue(fields.firstMatch.waitForExistence(timeout: 20), "connect screen did not appear")
        fields.element(boundBy: 0).tap()
        fields.element(boundBy: 0).typeText(conferenceId)
        // iOS shows a one-time tip about swipe typing on first use of the keyboard
        let tip = app.buttons["Continue"]
        if tip.waitForExistence(timeout: 3) { tip.tap() }
        // Return closes the keyboard, so later taps land on the fields and not on a scrolled form under it
        app.typeText("\n")
        app.buttons["Check"].tap()
        XCTAssertTrue(
            app.descendants(matching: .any).matching(NSPredicate(format: "label BEGINSWITH 'Found'")).firstMatch
                .waitForExistence(timeout: 40),
            "the feed was not found"
        )
    }

    /// False when the Open button could not be reached, so a layout problem shows up as a finding, not a crash.
    private func openConference() -> Bool {
        let name = app.textViews["Name of the conference"]
        // At large text sizes the name field is below the fold
        for _ in 0..<5 where !name.isHittable { app.swipeUp() }
        guard name.waitForExistence(timeout: 5) else { return false }
        name.tap()
        sleep(1)
        app.typeText("AuditTest\n")
        let open = app.buttons["Open the conference"]
        for _ in 0..<5 where !open.isHittable { app.swipeUp() }
        guard open.waitForExistence(timeout: 5) else {
            print("HIERARCHY-before-open\n\(app.debugDescription)")
            return false
        }
        open.tap()
        sleep(6)
        return true
    }

    private func walk(largeText: Bool, dark: Bool = false, name: String) {
        launch(largeText: largeText, dark: dark)
        _ = app.wait(for: .runningForeground, timeout: 20)
        sleep(3)
        audit("connect-empty")
        print("HIERARCHY-connect-empty\n\(app.debugDescription)")
        report("\(name)-connect-empty")
        findings = []
        connect()
        audit("connect-found")
        report("\(name)-connect-found")
        findings = []
        guard openConference() else {
            findings.append("could not reach and tap 'Open the conference' after entering a name; the form changed or is not scrollable to it")
            report("\(name)-stopped")
            return
        }
        audit("notifications-onboarding")
        let start = app.buttons["Let’s get started!"]
        // At large text sizes the button is below the fold
        for _ in 0..<5 where !start.isHittable { app.swipeUp() }
        guard start.waitForExistence(timeout: 5), start.isHittable else {
            findings.append("could not reach 'Let’s get started!' on the notifications screen; check it by hand at this text size")
            report("\(name)-stopped-onboarding")
            return
        }
        start.tap()
        // The system asks for notification permission; allow it so the reminder UI is reachable
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let allow = springboard.alerts.buttons["Allow"]
        if allow.waitForExistence(timeout: 5) { allow.tap() }
        sleep(3)
        audit("main")
        // Every button in the bottom bar is a top-level screen
        let height = app.frame.height
        var seen = Set<String>(["main"])
        let tabs = app.buttons.allElementsBoundByIndex
            .filter { $0.exists && $0.frame.minY > height * 0.85 && !$0.label.isEmpty }
            .map { $0.label }
        print("TABS \(tabs)")
        for label in tabs where !seen.contains(label) {
            seen.insert(label)
            app.buttons[label].firstMatch.tap()
            sleep(2)
            audit("tab-\(label)")
        }
        report("\(name)-main")
    }

    func testAuditNormalSize() { walk(largeText: false, name: "normal") }

    func testAuditAccessibilityXL() { walk(largeText: true, name: "ax-xl") }
}
