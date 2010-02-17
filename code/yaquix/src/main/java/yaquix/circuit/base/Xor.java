package yaquix.circuit.base;


/**
 * This base circuit is the xor of two inputs.
 * @author hk
 *
 */
public class Xor extends BinaryGateBase {

	@Override
	protected boolean getTableValue(int firstInput, int secondInput) {
		return (firstInput == 1) ^ (secondInput == 1);
	}
}
