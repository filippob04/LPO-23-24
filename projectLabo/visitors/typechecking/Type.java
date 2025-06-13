package projectLabo.visitors.typechecking;

public interface Type {
	default void checkEqual(Type found) {
		if (!equals(found))
			throw new TypecheckerException(found.toString(), toString());
	}

	default PairType toPairType() {
		throw new TypecheckerException(toString(), PairType.TYPE_NAME);
	}

	default DictType toDictType(Type found) { // Nuovo metodo per definire il tipo dictType (evito cast ripetuti)
    	throw new TypecheckerException(toString(), "Found " + found + " , expected " + DictType.TYPE_NAME);
	}		

}
