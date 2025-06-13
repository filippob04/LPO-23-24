
# 📚Relazione Progetto




## 🧑‍💻 Membri

- [Baldini Filippo - 6393212](6393212@studenti.unige.it)
- [Giacomo Cerlesi - 6364436](6364436@studenti.unige.it)

## How to compile:
```text
bash script.sh -c
```
### 🗓️ 14/05/2025
#### 📝 Grammatica del Linguaggio 

```text
Prog ::= StmtSeq EOF  
StmtSeq ::= Stmt (STMT_SEP StmtSeq)?  
Stmt ::= VAR? IDENT ASSIGN Exp | PRINT Exp | IF (Exp) Block (ELSE Block)? | FOR (VAR IDENT Of Exp) Block  
Block ::= OPEN_BLOCK StmtSeq CLOSE_BLOCK  
Exp ::= And (PAIR_OP And)*  
And ::= Eq (AND Eq)*  
Eq ::= Add (EQ Add)*  
Add ::= Mul (PLUS Mul)*  
Mul ::= Unary (TIMES Unary)*  
Unary ::= FST Unary | SND Unary | MINUS Unary | NOT Unary | Dict  
Dict ::= Atom ( [ Exp ( : Exp? )? ] )*  
Atom ::= [ Exp : Exp ] | BOOL | NUM | IDENT | ( Exp )  
```
#### 📑 Commenti
```text

> Abbiamo adattato il codice dello scorso laboratorio, implementando i nuovi costrutti richiesti;

> In particolare il ciclo for, a partire da parseStmt() con il caso della keyword FOR, che lancia il metodo parseForStmt() 
che è in grado di analizzare correttamente lo statement del ciclo for e genera il corrispettivo AST ForStmt.
ForStmt che ha un suo costruttore, metodo to string, visitor dedicato e variabili private.

> In secondo luogo abbiamo adattato il parser per la gestione del tipo Unary e il derivato Dict, dizionario 
che considerati degli indici di tipo Intero va a creare un dizionario con i suoi metodi nel file Dict.java 
(AST del dizionario) di GET, UPDATE, DELETE e CREATE (definiti in un enum) 
e i Metodi Getter anche se forse superflui, Visitor e toString adatti.

```

```text

> In seconda battuta abbiamo gestito il typechecking statico, in particolare con una semplice implementazione 
del visitor dedicato al ciclo for e una più complessa per il tipo Dictionary.

> Infatti abbiamo dovuto implementare un nuovo tipo derivato da Type: DictType necessario al visitor visitDict 
con il metodo toString con la formattazione richiesta, metodi getter, checkEquals e costruttore.

> Nel visitor associato risiede la logica dedicata al typechecking, 
in particolare si verifica innanzitutto che il dizionario sia effettivamente del tipo DictType, 
in seguito si verifica che key, indice del dizionario, sia un Intero, 
poi che il valore da inserire sia coerente con quelli già presenti all'interno del dizionario 
e infine che l'operazione da svolgere sul di esso sia fra quelle incluse nell'ENUM.

```

### 🗓️ 21/05/2025
#### 📑 Commenti
```text

> In primo luogo abbiamo completato il file execute.java e con se la semantica dimanica del tipo dict e del ciclo for.

> Successivamente completata una prima versione del progetto, tramite l'ausilio di uno script Bash,
abbiamo svolto una serie di test incontrando così alcuni problemi, in primo luogo il formatting
di alcuni messaggi di Errore lanciati dalle eccezioni da noi create.

> L'errore forse più notevole è stato interpretare "Dict" come keyword dedicata (non presente poi al di fuori della gramamtica CF),
successivamente abbiamo provato ad assegnarli il symbolo "[" come Open_Dict e "]" Close_Dict ma il problema persisteva
dunque alla fine abbiamo deciso di risolverlo riconoscendo il tipo dizionario tramite Atom -> (Atom ( [ Exp ( : Exp? )? ] )*),
un Dizionario inizia obbligatoriamente con Atom nella grammatica CF e dunque il constro controllo sta semplicemente
nel verificare che Atom sia equivalente ad Atom ::= [ Exp : Exp ].

```

### 🗓️ 22/05/2025
#### 📑 Commenti
```text

> Abbiamo dedicato questa giornata al debug del codice fin ora svolto, in particolare, test dopo test, ci siamo assicurati
che tutti i test passassero senza alcun problema. Per procedere con i debug (esistono vari commit definiti come savepoints)
abbiamo utilizzato principalmente le eccezioni, definendo nel loro corpo il nome del file di origine, in tal modo
siamo stati in grado di tracciare la maggior parte dei bug alla loro origine.

> Nello specifico, il parser gestiva male parseMinus(); in particolare, al posto di chiamare parseAtom() era necessario
utilizzare parseDict() (che include a sua volta parseAtom()) per affrontare casi in cui troviamo, dopo una Variabile negativa,
un dizionario. Tale modifica e' stata successivamente estesa a NOT Unary.

> Un secondo bug che abbiamo risolto era quello definito in dynamic-semantics-only. Il nostro programma lanciava una eccezione
nullptr, in particolare Delete non verificava la presenza di una chiave prima di tentare la sua eliminazione ed essa, 
assieme alla Update, ritornava null al posto di un dizionario effettivo. Il tipo di ritorno della Delete e Update
poi e' diverso dalla Create, Infatti, la Create crea un dizionario da zero (dictExp e' null e esiste un controllo dedicato),
nel caso delle altre due operazioni viene restituita una copia del dizionario (operazione definita nella classe DictValue.java)
in tal modo viene rispettata la specifica richiesta dal progetto.

> Infine e' stato aggiornato il metodo toString del dizionario che rispetta la formattazione descritta nel file di testing.
```

### 🧪 Testing
```text

> Abbiamo creato dei semplici .txt di testing ispirati al ReadMe presente su gitHub e otteniamo i risultati attesi.
inoltre abbiamo perfezionato lo script di testing in modo tale che determini autonomamente il numero di file di
testing presenti in ciascuna cartella

> prog01.txt:
    - Contiene un primo caso di testing (estrapolato dal readMe) che verifica il funzionamento dell'opzione -ntc
    - In secondo luogo abbiamo inserito un divertente esempio di funzione che utilizza il ciclo enhanced for 
        per calcolare la sequenza di fibonacci. (poiche' il tipo di Exp dev'essere Dict siamo dovuti scendere a compromessi)

> prog02.txt:
    - Contiene un primo caso di testing (estrapolato dal readMe) che verifica il funzionamento del ciclo for
    - Successivamente abbiamo incluso una semplice funzione di somma di tutti gli elementi di un dizionario,
      assieme ad un banalissimo e superfluo if statement.

> prog03.txt:
    - Viene verificato il funzionamento dell' -ntc, se abilitato viene ignorato il vincolo di omogeneita' delle variabili

> prog04.txt:
    - Creiamo un Dizionario di Dizionari, ossia una matrice NxM. NB: Gli indici dei dizionari risultano indipendenti.

> prog05.txt:
    - Viene verificato il vincolo di integrita' del tipo delle chiavi 
      (Intero, anche con -ntc (come specificato nel readMe))

> prog06.txt:
    - Viene verificato il vincolo di integrita' del tipo del ciclo for 
      (Dizionario, anche con -ntc (come specificato nel readMe))

> prog07.txt
    - Verifica il funzionamento dell'operazione di get da un dizionario e utilizza tale valore 
      come membro di un secondo dizionario

> prog08.txt
    - Una versione piu' complessa del test precedente, si crea un dizionario di dizionari preesistenti

> prog09.txt
    - Si verifica l'omogeneita' dei valori di un dizionario, con -ntc tale check
      non viene effettuato e dunque viene stampato il valore effettivo.

```











