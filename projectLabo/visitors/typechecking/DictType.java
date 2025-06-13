package projectLabo.visitors.typechecking;

public class DictType implements Type {

    public static final String TYPE_NAME = "DICT";
    private final Type keyType;   // Tipo delle chiavi nel dizionario (dev'essere INT)
    private final Type valueType; // Tipo dei valori nel dizionario (dev'essere coerente)

    
    public DictType(Type keyType, Type valueType){ // Costruttore
        this.keyType = keyType;
        this.valueType = valueType;
    }

    // Metodi Getter
    public Type getKeyType(){ // Dovrebbe sempre ritornare INT
        return keyType;
    }

    public Type getValueType(){
        return valueType;
    }

    
    @Override
    public void checkEqual(Type other){ // Metodo per confrontare due dizionari
        if(!(other instanceof DictType)){
            throw new TypecheckerException("Found " + other + " , expected " + valueType + " DICT");
        }
        DictType otherDict = other.toDictType(other);      // Come nel visitor
        // keyType.checkEqual(otherDict.getKeyType());     // Verifica che i tipi delle chiavi siano uguali (Inutile poiche' entrambi INT)

       try{
        valueType.checkEqual(otherDict.getValueType()); // Verifica di coerenza dei valori
       } catch (TypecheckerException e) {
            throw new TypecheckerException("Found " + otherDict.getValueType() + " DICT, expected " + valueType + " DICT");
       }
    }

    // Metodo toString
    @Override
    public String toString(){
        return "[" + keyType + ":" + valueType + "]";
    }

    @Override // Override del metodo toDictType definito in Type.java, come per PairType!!!
    public DictType toDictType(Type found) {
        // System.out.println("DEBUG :: DictType.java");
        return this; // già un dizionario valido, quindi si restituisce sé stesso
    }
}
