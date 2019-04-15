package generator.assertion;

/**
 * <p>OutputTraceVisitor interface.</p>
 *
 * @author fraser
 */
public interface OutputTraceVisitor<T extends OutputTraceEntry> {

	public void visit(T entry);
}
