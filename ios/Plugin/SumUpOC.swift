import Foundation

@objc public class SumUpOC: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
