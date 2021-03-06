package jsfacile

import scala.collection.immutable

import jsfacile.api.{Appender, Parser, parserOf}

package object jsonast {

	sealed trait JsValue
	case class JsObject(fields: immutable.Map[String, JsValue]) extends JsValue
	case class JsArray(array: immutable.Iterable[JsValue]) extends JsValue
	case class JsString(string: String) extends JsValue
	case class JsNumber(number: BigDecimal) extends JsValue
	object JsBoolean {def apply(value: Boolean): JsBoolean = if (value) JsTrue else JsFalse}
	sealed trait JsBoolean extends JsValue {def value: Boolean}
	case object JsTrue extends JsBoolean {override def value = true}
	case object JsFalse extends JsBoolean {override def value = false}
	case object JsNull extends JsValue

	//// Parsers ////

	implicit val jpAstNull: Parser[JsNull.type] = Parser.expectStr("null", JsNull)
	implicit val jpAstFalse: Parser[JsFalse.type] = Parser.expectStr("false", JsFalse)
	implicit val jpAstTrue: Parser[JsTrue.type] = Parser.expectStr("true", JsTrue)
	implicit val jpAstBoolean: Parser[JsBoolean] = { cursor =>
		if (cursor.comes("true")) JsTrue
		else if (cursor.comes("false")) JsFalse
		else {
			cursor.miss("A boolean was expected")
			JsFalse
		}
	}
	implicit val jpAstNumber: Parser[JsNumber] = parserOf[BigDecimal] ^^ JsNumber.apply
	implicit val jpAstString: Parser[JsString] = parserOf[String] ^^ JsString.apply
	implicit val jpAstArray: Parser[JsArray] = parserOf[immutable.Iterable[JsValue]] ^^ JsArray.apply
	implicit val jpAstObject: Parser[JsObject] = parserOf[immutable.Map[String, JsValue]] ^^ JsObject.apply

	implicit lazy val jpAstValue: Parser[JsValue] = Parser.pick >> { char =>
		val x: Parser[_ <: JsValue] = char match {
			case '"' => jpAstString
			case '{' => jpAstObject
			case '[' => jpAstArray
			case 't' => jpAstTrue
			case 'f' => jpAstFalse
			case 'n' => jpAstNull
			case _ => jpAstNumber
		}
		x.asInstanceOf[Parser[JsValue]]
	}

	//// Appenders ////

	implicit val jaAstNull: Appender[JsNull.type] = (r, _) => r.append("null");
	implicit val jaAstTrue: Appender[JsTrue.type] = (r, _) => r.append("true");
	implicit val jaAstFalse: Appender[JsFalse.type] = (r, _) => r.append("false");
	implicit val jaAstBoolean: Appender[JsBoolean] = (r, a) => r.append {if (a.value) "true" else "false"}
	implicit val jaAstNumber: Appender[JsNumber] = (r, a) => r.appendSummoned[BigDecimal](a.number)
	implicit val jaAstString: Appender[JsString] = (r, a) => r.appendSummoned[String](a.string)
	implicit val jaAstArray: Appender[JsArray] = (r, a) => r.appendSummoned[immutable.Iterable[JsValue]](a.array)
	implicit val jaAstObject: Appender[JsObject] = (r, a) => r.appendSummoned[immutable.Map[String, JsValue]](a.fields)
	implicit lazy val jaAstValue: Appender[JsValue] = { (r, a) =>
		val appender: Appender[_ <: JsValue] = a match {
			case JsNull => jaAstNull
			case JsTrue => jaAstTrue
			case JsFalse => jaAstFalse
			case JsNumber(_) => jaAstNumber
			case JsString(_) => jaAstString
			case JsArray(_) => jaAstArray
			case JsObject(_) => jaAstObject
		}
		appender.asInstanceOf[Appender[JsValue]].append(r, a)
	}
}
