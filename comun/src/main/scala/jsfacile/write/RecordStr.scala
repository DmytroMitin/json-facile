package jsfacile.write

class RecordStr(val sb: java.lang.StringBuilder) extends Record {

	override def appendCodePoint(codePoint: Int): this.type = {
		sb.appendCodePoint(codePoint)
		this
	};
	override def append(string: String): this.type = {
		sb.append(string);
		this;
	}
	override def append(int: Int): this.type = {
		sb.append(int);
		this;
	}
	override def append(long: Long): this.type = {
		sb.append(long)
		this;
	}
	override def append(float: Float): this.type = {
		sb.append(float)
		this;
	}
	override def append(double: Double): this.type = {
		sb.append(double)
		this;
	}
	override def append(csq: CharSequence): this.type = {
		sb.append(csq)
		this;
	}
	override def append(csq: CharSequence, start: Int, end: Int): this.type = {
		sb.append(csq, start, end)
		this;
	}
	override def append(c: Char): this.type = {
		sb.append(c)
		this;
	}
}
