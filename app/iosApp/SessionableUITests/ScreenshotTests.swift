import XCTest

/// Captures the app's main screens as PNGs for the README and the forking guide. Not a pass/fail test: run it by hand
/// (see docs/FORKING.md). Works for the generic build (starts on the connect screen) and for a branded build (starts
/// on the notifications onboarding). Files go to /private/tmp/sessionable-shots/<prefix>-<screen>.png.
@MainActor
final class ScreenshotTests: XCTestCase {
    private let app = XCUIApplication()
    private let conferenceId = "asqam7rn"
    private let directory = URL(fileURLWithPath: "/private/tmp/sessionable-shots")

    override func setUp() { continueAfterFailure = true }

    private func snap(_ prefix: String, _ screen: String) {
        try? FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        let url = directory.appendingPathComponent("\(prefix)-\(screen).png")
        try? app.screenshot().pngRepresentation.write(to: url)
    }

    func testCaptureScreens() {
        app.launchArguments = ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        app.launch()

        let generic = app.textViews.firstMatch.waitForExistence(timeout: 15)
        let prefix = generic ? "generic" : "branded"
        if generic {
            sleep(2)
            snap(prefix, "01-connect")
            let link = app.textViews.element(boundBy: 0)
            link.tap()
            link.typeText(conferenceId)
            let tip = app.buttons["Continue"]
            if tip.waitForExistence(timeout: 3) { tip.tap() }
            app.typeText("\n")
            app.buttons["Check"].tap()
            _ = app.descendants(matching: .any).matching(NSPredicate(format: "label BEGINSWITH 'Found'"))
                .firstMatch.waitForExistence(timeout: 40)
            let name = app.textViews["Name of the conference"]
            for _ in 0..<4 where !name.isHittable { app.swipeUp() }
            name.tap()
            sleep(1)
            app.typeText("AGNTCon + MCPCon 2026\n")
            for _ in 0..<4 where !app.buttons["Open the conference"].isHittable { app.swipeDown() }
            snap(prefix, "02-connect-found")
            let open = app.buttons["Open the conference"]
            for _ in 0..<4 where !open.isHittable { app.swipeUp() }
            open.tap()
        }

        let start = app.buttons["Let’s get started!"]
        XCTAssertTrue(start.waitForExistence(timeout: 20), "onboarding did not appear")
        snap(prefix, "03-onboarding")
        start.tap()
        let allow = XCUIApplication(bundleIdentifier: "com.apple.springboard").alerts.buttons["Allow"]
        if allow.waitForExistence(timeout: 5) { allow.tap() }
        sleep(4)

        let height = app.frame.height
        let tabs = app.buttons.allElementsBoundByIndex
            .filter { $0.exists && $0.frame.minY > height * 0.85 && !$0.label.isEmpty }
            .map { $0.label }
        for (index, label) in tabs.enumerated() {
            app.buttons[label].firstMatch.tap()
            sleep(3)
            snap(prefix, String(format: "%02d-%@", 10 + index, label.lowercased()))
        }
    }
}
