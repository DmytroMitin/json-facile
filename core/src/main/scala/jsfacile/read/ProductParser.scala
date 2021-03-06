package jsfacile.read

import scala.collection.immutable.ArraySeq

import jsfacile.macros.{ProductParserHelper, ProductUpperBound}
import jsfacile.macros.ProductParserHelper.PphFieldInfo
import jsfacile.read.Parser._
import jsfacile.util.BinarySearch

class ProductParser[P <: ProductUpperBound](helper: ProductParserHelper[P]) extends Parser[P] {

	assert(helper != null); // Fails here when the macro expansion of ProductParserHelper fails for some reason. Usually because a compilation error of the expanded code. To find the place in the log search the string "<empty>"

	override def parse(cursor: Cursor): P = {

		if (cursor.have) {
			if (cursor.pointedElem == '{') {
				cursor.advance();
				val fieldsCount = helper.fieldsInfo.length;
				val ctorArgs = new Array[Any](fieldsCount);
				val fieldsFound = new Array[Boolean](fieldsCount);

				var have = cursor.consumeWhitespaces();
				while (have && cursor.pointedElem != '}') {
					have = false;
					val fieldName = jpString.parse(cursor);
					if (cursor.consumeWhitespaces() &&
						cursor.consumeChar(':') &&
						cursor.consumeWhitespaces()
					) {
						val fieldInfo = BinarySearch.find[PphFieldInfo[_]](helper.fieldsInfo) {_.name.compare(fieldName)}
						if (fieldInfo == null) {
							Skip.jsValue(cursor);
						} else {
							val fieldValue = fieldInfo.valueParser.parse(cursor);
							ctorArgs(fieldInfo.ctorArgIndex) = fieldValue;
							fieldsFound(fieldInfo.ctorArgIndex) = true;
						}
						have = cursor.consumeWhitespaces() && (cursor.pointedElem == '}' || (cursor.consumeChar(',') && cursor.consumeWhitespaces()))
					}
				}
				if (have) {
					cursor.advance();

					var fieldIndex = 0;
					while (fieldIndex < fieldsCount && cursor.ok) {
						if (!fieldsFound(fieldIndex)) {
							val fieldInfo = helper.fieldsInfo(fieldIndex);
							fieldInfo.oDefaultValue match {
								case Some(defaultValue) =>
									ctorArgs(fieldIndex) = defaultValue
								case None =>
									cursor.miss(s"""Missing required field "${fieldInfo.name}" when trying to parse a "${helper.fullName}""")
							}
						}
						fieldIndex += 1;
					}
					if (cursor.ok) {
						return helper.createProduct(ArraySeq.unsafeWrapArray(ctorArgs))
					}
				} else {
					cursor.fail(s"Invalid syntax for an object while parsing a ${helper.fullName}");
				}
			} else {
				cursor.miss();
			}
		} else {
			cursor.miss(s"A '{' expected but the end of the content was reached when trying to parse a ${helper.fullName}")
		}
		ignored[P]
	}
}

