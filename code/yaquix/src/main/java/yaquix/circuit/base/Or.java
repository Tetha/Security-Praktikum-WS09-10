package yaquix.circuit.base;

/**
 * This base circuit is the or of two inputs.
 * @author hk
 *
 */
public class Or extends BinaryGateBase {
	@Override
	protected boolean getTableValue(int firstInput, int secondInput) {
		return (firstInput == 1 || secondInput == 1);
	}
}
