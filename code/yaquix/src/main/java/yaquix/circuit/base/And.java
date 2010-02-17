package yaquix.circuit.base;

/**
 * This base circuit is the and of two inputs.
 * @author hk
 *
 */
public class And extends BinaryGateBase {
	@Override
	protected boolean getTableValue(int firstInput, int secondInput) {
		if (firstInput == 1 && secondInput == 1) {
			return true;
		} else {
			return false;
		}
	}
}
