package idl.bdp.scrashlocator.bucketing.json

  sealed trait Json
  /* PARSING CLASS FOR ECLIPSE REPORT FORMAT */
  case class MethodCalled(method: String, source: String) extends Json
  case class StackTraceInfo(exceptionType: String, elements: List[MethodCalled], number: String, commentIndex: String, bugId: String, date: String, product: String, component: String, severity: String) extends Json
  case class BugReport(comments: List[String], commentCreationDates: List[String], traces: List[StackTraceInfo], groupId: String, bugId: String, date: String, product: String, component: String, severity: String) extends Json