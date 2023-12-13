package pl.janczaja;

import org.apache.commons.math3.distribution.PoissonDistribution;

import java.lang.reflect.Array;
import java.util.*;

public class ProblemGenerator {

    // TESTOWANIE
    public void test1() {
        Clause clause1 = generateClause(10);
        System.out.println(clause1);

        Clause clause2 = generateSafetyClause(10);
        System.out.println(clause2);

        Clause clause3 = generateLivenessClause(10);
        System.out.println(clause3);
    }

    public void test2() {
        System.out.println("generateProblem1");
        generateProblem1(new ArrayList<Integer>(Arrays.asList(2, 4, 6, 8, 10, 12)), 10, Optional.empty(), Optional.empty());
        System.out.println(formula);

        System.out.println("generateProblem2");
        generateProblem2(10, Optional.empty(), Optional.empty());
        System.out.println(formula);

        System.out.println("generateProblem3");
        generateProblem3(new ArrayList<Integer>(Arrays.asList(2, 4, 6, 8, 10, 12)), 10, 15);
        System.out.println(formula);

        System.out.println("generateProblem4");
        generateProblem4(2, 10, Optional.empty());
        System.out.println(formula);

        System.out.println("generateProblem5");
        generateProblem5(new ArrayList<Integer>(Arrays.asList(2, 4, 6, 8, 10, 12)), 10, "even", Optional.empty());
        System.out.println(formula);

        System.out.println("generateProblem6 - poison");
        generateProblem6(2, 10, 0.5, true);
        System.out.println(formula);

        System.out.println("generateProblem6 - no poison");
        generateProblem6(2, 10, 0.5, false);
        System.out.println(formula);

        System.out.println("generateProblem7 - poison");
        generateProblem7(2, 10, true);
        System.out.println(formula);

        System.out.println("generateProblem7 - no poison");
        generateProblem7(2, 10, false);
        System.out.println(formula);

        System.out.println("generateProblem8 - poison");
        generateProblem8(2, 10, true);
        System.out.println(formula);

        System.out.println("generateProblem8 - no poison");
        generateProblem8(2, 10, false);
        System.out.println(formula);
    }

//    void test3() {
//        System.out.println("generateProblem1");
//        generateProblem1(new ArrayList<Integer>(Arrays.asList(2, 4, 6, 8, 10, 12)), 20, Optional.empty(), Optional.empty());
//        System.out.println(formula);
//
//        translateToZ3(this.formula);
//    }

    //

    Random random;

    // Przypisane w konstruktorze.

    String test_type;
    Double lambda_value;

    ArrayList<Clause> formula = new ArrayList<>();
    ArrayList<Clause> formula2 = new ArrayList<>();
    ArrayList<Clause> formula3 = new ArrayList<>();
    ArrayList<Clause> formulaR = new ArrayList<>();

    //
    Map<String, Integer> atom_counting_dict = new HashMap<>();

    String output_file_name;


    ProblemGenerator(String test_type,
                     Optional<Integer> precentage_of_safty_clauses_optional,
                     Optional<int[]> clause_lengths_optional,
                     Optional<Integer> clauses_num_optional,
                     Optional<Boolean> poisson_optional,
                     Optional<Double> atoms_num_coeff_optional,
                     Optional<String> problem5_distribution_optional,
                     Optional<Double> lambda_value_optional) {

        // Wartości domyślne.
        Integer precentage_of_safty_clauses = precentage_of_safty_clauses_optional.orElse(50);
        int[] clause_lengths = clause_lengths_optional.orElse(new int[]{0, 2, 3, 4, 6, 8, 10});
        Integer clauses_num = clauses_num_optional.orElse(50);
        Boolean poisson = poisson_optional.orElse(false);
        Double atoms_num_coeff = atoms_num_coeff_optional.orElse(0.5);
        String problem5_distribution = problem5_distribution_optional.orElse("even");
        Double lambda_value = lambda_value_optional.orElse(3.5);

        //

        this.random = new Random();
        this.test_type = test_type;
        this.lambda_value = lambda_value;

        //
        long atoms_num = Math.round(clauses_num * atoms_num_coeff);

        for (int i = 0; i < atoms_num + 1; i++) {
            String atom_name = "var" + i;
            this.atom_counting_dict.put(atom_name, 0);
        }

        // Tworzenie nazwy pliku
        StringBuilder file_name_builder = new StringBuilder();
        file_name_builder.append(test_type);
        file_name_builder.append("_c");
        file_name_builder.append(clauses_num);
        file_name_builder.append("_a");
        file_name_builder.append(atoms_num);
        file_name_builder.append("_prec");
        file_name_builder.append(precentage_of_safty_clauses);
        file_name_builder.append("_lengths");
        for (int constant : clause_lengths) {
            file_name_builder.append("_");
            file_name_builder.append(constant);
        }
        if (poisson) {
            file_name_builder.append("_poisson");
        } else {
            file_name_builder.append("_distribution_");
            file_name_builder.append(problem5_distribution);

        }
        this.output_file_name = String.valueOf(file_name_builder);

//        generate();
//
//        cleanup();
//
//        saveFormulas();
    }

    void generate(int precentage_of_safty_clauses,
                  ArrayList<Integer> clause_lengths,
                  int clauses_num,
                  boolean poisson,
                  double atoms_num_coeff,
                  String problem5_distribution) {

        if (!this.test_type.equals("problem3") && atoms_num_coeff != 0.5) {
            throw new RuntimeException(String.format("Generator.generate: Test of type %s should have atoms number coefficient equal to 0.5.", this.test_type));
        }

        // TODO: WARUNEK DO SPRAWDZENIA I PORÓWNANIA Z ORGINAŁEM
        if (!(this.test_type.equals("problem4") || this.test_type.equals("problem5")) && clause_lengths.size() <= 1) {
            throw new RuntimeException(String.format("Generator.generate: Test of type %s should have more than one clause length given.", this.test_type));
        }

        if (!this.test_type.equals("problem6") && precentage_of_safty_clauses != 50) {
            throw new RuntimeException(String.format("Generator.generate: Test of type %s should have precentage of safety clauses equal to 50.", this.test_type));
        }

        if (Arrays.asList("problem1", "problem3", "problem4", "problem5").contains(this.test_type) && poisson) {
            throw new RuntimeException(String.format("Generator.generate: Test of type %s do not support poisson distribution.", this.test_type));
        }

        // TODO: DODAWAĆ KOLEJNE WYWOŁANIA
        // TUTAJ SĄ WYWOŁANIA KOLEJNYCH FUNKCJI GENERUJĄCYCH (patrz ogrinał drabinka if else)
        switch (this.test_type) {
            case "problem1" -> generateProblem1(clause_lengths, clauses_num, Optional.empty(), Optional.empty());

        }
    }

    private void generateProblem1(ArrayList<Integer> clauses_lengths, int clauses_num, Optional<Double> safety_coeff_optional, Optional<Integer> target_formula_optional) {
        // Wartości domyślne;
        Double safety_coeff = safety_coeff_optional.orElse(0.5);
        Integer target_formula = target_formula_optional.orElse(1);


        ArrayList<Clause> formula = new ArrayList<>();

        if (clauses_lengths.size() > clauses_num) {
            throw new RuntimeException("Generator.generateProblem1: The number of required different clauses lengths is higher than the number of clauses.");
        }

        long clauses_of_one_length_number = Math.round((float) clauses_num / clauses_lengths.size());
        long safety_clauses_number = Math.round(clauses_of_one_length_number * safety_coeff);

        for (int current_length : clauses_lengths) {
            for (int clause_of_current_length_idx = 0; clause_of_current_length_idx < clauses_of_one_length_number; clause_of_current_length_idx++) {
                if (formula.size() < clauses_num) {
                    if (clause_of_current_length_idx < safety_clauses_number) {
                        Clause clause = generateSafetyClause(current_length);
                        formula.add(clause);
                    } else {
                        Clause clause = generateLivenessClause(current_length);
                        formula.add(clause);
                    }
                }
            }
        }

        int clauses_shortage = clauses_num - formula.size();

        long limit = Math.round(clauses_shortage * safety_coeff);

        for (int counter = 0; counter < clauses_shortage; counter++) {
            if (counter <= limit) {
                Clause clause = generateSafetyClause(clauses_lengths.get(random.nextInt(clauses_lengths.size())));
                formula.add(clause);
            } else {
                Clause clause = generateLivenessClause(clauses_lengths.get(random.nextInt(clauses_lengths.size())));
                formula.add(clause);
            }
        }


        switch (target_formula) {
            case 1 -> {
                this.formula.clear();
                this.formula.addAll(formula);
            }
            case 2 -> {
                this.formula2.clear();
                this.formula2.addAll(formula);
            }
            case 3 -> {
                this.formula3.clear();
                this.formula3.addAll(formula);
            }
        }
    }

    private void generateProblem2(int clauses_num, Optional<Double> safety_coeff_optional, Optional<Integer> target_formula_optional) {
        // Wartości domyślne;
        Double safety_coeff = safety_coeff_optional.orElse(0.5);
        Integer target_formula = target_formula_optional.orElse(1);

        ArrayList<Clause> formula = new ArrayList<>();
        ArrayList<Integer> clauses_lengths = new ArrayList<>();
        ArrayList<Double> probabilities = new ArrayList<>();

        PoissonDistribution poissonDist = new PoissonDistribution(this.lambda_value);

        for (int length = 1; length <= clauses_num; length++) {
            double pmfValue = poissonDist.probability(length);
            int roundedPmf = (int) Math.round(pmfValue * clauses_num);

            // TODO: do sprawdzenia
            if (roundedPmf == 0 && length > this.lambda_value) {
                break;
            }

            clauses_lengths.add(length);
            probabilities.add(pmfValue);
        }

        // Odpowiednik: clauses_of_each_length_numbers = [round(prob*clauses_num) for prob in probabilities] TODO: Do sprawdzenia
        ArrayList<Integer> clauses_of_each_length_numbers = new ArrayList<>();
        for (double prob : probabilities) {
            int roundedValue = (int) Math.round(prob * clauses_num);
            clauses_of_each_length_numbers.add(roundedValue);
        }

        // Odpowiednik: safety_clauses_numbers = [round(number*safety_coeff) for number in clauses_of_each_length_numbers] TODO: Do sprawdzenia
        ArrayList<Integer> safety_clauses_numbers = new ArrayList<>();
        for (int number : clauses_of_each_length_numbers) {
            int roundedValue = (int) Math.round(number * safety_coeff);
            safety_clauses_numbers.add(roundedValue);
        }

        for (int current_length_idx = 0; current_length_idx < clauses_lengths.size(); current_length_idx++) {
            for (int clause_of_current_length_idx = 0; clause_of_current_length_idx < clauses_of_each_length_numbers.get(current_length_idx); clause_of_current_length_idx++) {

                if (formula.size() < clauses_num) {
                    if (clause_of_current_length_idx < safety_clauses_numbers.get(current_length_idx)) {
                        Clause clause = generateSafetyClause(clauses_lengths.get(current_length_idx));
                        formula.add(clause);
                    } else {
                        Clause clause = generateLivenessClause(clauses_lengths.get(current_length_idx));
                        formula.add(clause);
                    }
                }
            }
        }

        int clauses_shortage = clauses_num - formula.size();

        long limit = Math.round(clauses_shortage * safety_coeff);

        // TODO: kopiowane. Do sprawdzenia.
        for (int counter = 0; counter < clauses_shortage; counter++) {
            if (counter <= limit) {
                Clause clause = generateSafetyClause(clauses_lengths.get(random.nextInt(clauses_lengths.size())));
                formula.add(clause);
            } else {
                Clause clause = generateLivenessClause(clauses_lengths.get(random.nextInt(clauses_lengths.size())));
                formula.add(clause);
            }
        }

        switch (target_formula) {
            case 1 -> {
                this.formula.clear();
                this.formula.addAll(formula);
            }
            case 2 -> {
                this.formula2.clear();
                this.formula2.addAll(formula);
            }
            case 3 -> {
                this.formula3.clear();
                this.formula3.addAll(formula);
            }
        }
    }

    private void generateProblem3(ArrayList<Integer> clause_lengths, int clauses_num, double atoms_num_coeff) {
        ArrayList<Integer> newClauseLength = new ArrayList<>();

        for (int length : clause_lengths) {
            if (length <= atoms_num_coeff) {
                newClauseLength.add(length);
            }
        }

        if (newClauseLength.isEmpty()) {
            throw new RuntimeException("Generator.generateProblem3: No usable lengths");
        }

        // Generowanie formuły jak dla problemu 1, używając nowych długości klauzul
        generateProblem1(newClauseLength, clauses_num, Optional.empty(), Optional.empty());
    }

    private void generateProblem4(Integer clause_length, int clauses_num, Optional<Double> safety_coeff_optional) {
        Double safety_coeff = safety_coeff_optional.orElse(0.5);

        long safety_clauses_number = Math.round(clauses_num * safety_coeff);

        for (int clause_number = 0; clause_number < clauses_num; clause_number++) {
            if (clause_number < safety_clauses_number) {
                Clause clause = generateSafetyClause(clause_length);
                this.formula.add(clause);
            } else {
                Clause clause = generateLivenessClause(clause_length);
                this.formula.add(clause);
            }
        }
    }

    private void generateProblem5(ArrayList<Integer> clauses_lengths, int clauses_num, String distribution, Optional<Double> safety_coeff_optional) {
        Double safety_coeff = safety_coeff_optional.orElse(0.5);

        if (clauses_num < 4) {
            throw new RuntimeException("Generator.generateProblem5: The number of required different clauses lengths is higher than the number of clauses.");
        }

        List<Integer> clausesOfEachLengthNumbers = new ArrayList<>();
        switch (distribution) {
            case "even" -> {
                for (int i = 0; i < 4; i++) {
                    int roundedValue = Math.round(clauses_num / 4.0f);
                    clausesOfEachLengthNumbers.add(roundedValue);
                }
            }
            case "more_long" -> {
                for (int i = 0; i < 4; i++) {
                    int roundedValue;
                    if (i == 0) {
                        roundedValue = Math.round(clauses_num * 0.01f + 0.001f);
                    } else {
                        roundedValue = Math.round(clauses_num * 0.33f);
                    }
                    clausesOfEachLengthNumbers.add(roundedValue);
                }
            }
            case "more_short" -> {
                for (int i = 0; i < 4; i++) {
                    int roundedValue;
                    if (i == 3) {
                        roundedValue = Math.round(clauses_num * 0.01f + 0.001f);
                    } else {
                        roundedValue = Math.round(clauses_num * 0.33f);
                    }
                    clausesOfEachLengthNumbers.add(roundedValue);
                }
            }
            default -> {
                throw new RuntimeException("Generator.generateProblem5: Invalid distribution. Choose from [\\\"even\\\",\\\"more_long\\\",\\\"more_short\\\"]\"");
            }
        }

        List<Integer> safetyClausesNumber = new ArrayList<>();
        for (int number : clausesOfEachLengthNumbers) {
            int roundedValue = ((int) Math.round(number * safety_coeff));
            safetyClausesNumber.add(roundedValue);
        }

        Collections.sort(clauses_lengths);

        // TODO: Generowane. Do sprawdzenia.
        for (int currentLengthIdx = 0; currentLengthIdx < 4; currentLengthIdx++) {
            for (int clauseOfCurrentLengthIdx = 0; clauseOfCurrentLengthIdx < clausesOfEachLengthNumbers.get(currentLengthIdx); clauseOfCurrentLengthIdx++) {
                if (formula.size() < clauses_num) {
                    int currentLength = clauses_lengths.get(currentLengthIdx);

                    Clause clause;
                    if (clauseOfCurrentLengthIdx < safetyClausesNumber.get(currentLengthIdx)) {
                        clause = generateSafetyClause(currentLength);
                    } else {
                        clause = generateLivenessClause(currentLength);
                    }

                    this.formula.add(clause);
                }
            }
        }

        int clauses_shortage = clauses_num - this.formula.size();

        long limit = Math.round(clauses_shortage * safety_coeff);

        // TODO: Generowane. Do sprawdzenia.
        for (int counter = 0; counter < clauses_shortage; counter++) {
            if (counter <= limit) {
                Clause clause = generateSafetyClause(clauses_lengths.get(random.nextInt(clauses_lengths.size())));
                formula.add(clause);
            } else {
                Clause clause = generateLivenessClause(clauses_lengths.get(random.nextInt(clauses_lengths.size())));
                formula.add(clause);
            }
        }
    }

    private void generateProblem6(Integer clause_length, int clauses_num, Double safety_precentage, Boolean poisson) {
        if (safety_precentage < 0 || safety_precentage > 100) {
            throw new RuntimeException("Generator.generateProblem6: Safety clauses precentage not in range [0;100]");
        }

        if (poisson) {
            generateProblem2(clauses_num, Optional.of(safety_precentage / 100), Optional.empty());
        } else {
            generateProblem1(new ArrayList<>(List.of(clause_length)), clauses_num, Optional.of(safety_precentage / 100), Optional.empty());
        }
    }

    private void generateProblem7(Integer clause_length, int clauses_num, Boolean poisson) {
        if (poisson) {
            generateProblem2(clauses_num, Optional.empty(), Optional.of(1));
            generateProblem2(clauses_num, Optional.empty(), Optional.of(2));
            generateProblem2(clauses_num, Optional.empty(), Optional.of(3));
        } else {
            generateProblem1(new ArrayList<>(List.of(clause_length)), clauses_num, Optional.empty(), Optional.of(1));
            generateProblem1(new ArrayList<>(List.of(clause_length)), clauses_num, Optional.empty(), Optional.of(2));
            generateProblem1(new ArrayList<>(List.of(clause_length)), clauses_num, Optional.empty(), Optional.of(3));
        }

        generateFormulaR();
    }

    private void generateProblem8(Integer clause_length, int clauses_num, Boolean poisson) {
        if (poisson) {
            generateProblem2(clauses_num, Optional.empty(), Optional.of(1));
            generateProblem2(clauses_num, Optional.empty(), Optional.of(2));
        } else {
            generateProblem1(new ArrayList<>(List.of(clause_length)), clauses_num, Optional.empty(), Optional.of(1));
            generateProblem1(new ArrayList<>(List.of(clause_length)), clauses_num, Optional.empty(), Optional.of(2));
        }
    }

    // CHYBA GOTOWE
    private Clause generateSafetyClause(Integer clause_length) {
        Clause clause = generateClause(clause_length);
        clause.var.add(0, new LogicToken("FORALL", Optional.empty(), Optional.empty()));
        return clause;
    }

    private Clause generateLivenessClause(Integer clause_length) {
        Clause clause = generateClause(clause_length);

        if (clause.var.size() > 1) {
            replaceORwithIMP(clause);
        }

        clause.var.add(0, new LogicToken("EXISTS", Optional.empty(), Optional.empty()));

        return clause;
    }

    private Clause generateClause(Integer length) {
        ArrayList<LogicToken> atoms = getRandomAtomList(length);
        ArrayList<LogicToken> relations = new ArrayList<>();

        for (int i = 0; i < (length - 1); i++) {
            relations.add(getRandomRelation());
        }

        Clause clause = new Clause(Utils.zipToList(atoms, relations));

        return clause;
    }

    // CHYBA GOTOWE
    private ArrayList<LogicToken> getRandomAtomList(Integer length) {

        ArrayList<LogicToken> atoms = new ArrayList<>();

        ArrayList<String> avaliable_atoms = new ArrayList<>(this.atom_counting_dict.keySet());

        for (int i = 0; i < length; i++) {

            if (avaliable_atoms.isEmpty()) {
                throw new RuntimeException("Generator.getRandomAtomList: The number of atoms is smaller than length of clause.");
            }

            // Pobiera losową wartość z avaliable_atoms;
            String value = avaliable_atoms.get(random.nextInt(avaliable_atoms.size()));

            avaliable_atoms.remove(value);

            this.atom_counting_dict.put(value, this.atom_counting_dict.get(value) + 1);

            boolean negation = random.nextBoolean();

            atoms.add(new LogicToken("ATOM", Optional.ofNullable(value), Optional.of(negation)));
        }

        return atoms;
    }

    // CHYBA GOTOWE
    private LogicToken getRandomRelation() {
        return new LogicToken();
    }

    // DO SPRAWDZENIA
    private void replaceORwithIMP(Clause clause) {
        Random random = new Random();

        // Pick a token randomly
        LogicToken orToChange = clause.var.get(random.nextInt(clause.var.size()));

        // While it's not an OR token, pick another one
        // (the chances of picking an OR token are only a little less than 50%
        // so this method is not as inefficient as it looks)
        while (!"OR".equals(orToChange.getTokenType())) {
            orToChange = clause.var.get(random.nextInt(clause.var.size()));
        }

        // Change the token's type to IMP (all of its other fields are, and should stay, empty)
        orToChange.setTokenType("IMP");
    }

    // TODO: DO SPRAWDZENIA !!!!!!!!!!!!!!
    private void generateFormulaR() {
        this.formulaR.clear();
        this.formulaR.add(generateSafetyClause(4));
        this.formulaR.get(0).var.set(0, new LogicToken("EXISTS", Optional.empty(), Optional.empty()));
    }

    private void cleanup(Double precentage_of_safty_clauses){

        ArrayList<ArrayList<Clause>> list_of_formulas = new ArrayList<>();

        if(this.test_type.startsWith("problem7")){
            list_of_formulas.add(formula);
            list_of_formulas.add(formula2);
            list_of_formulas.add(formula3);
        } else if(this.test_type.startsWith("problem8")){
            list_of_formulas.add(formula);
            list_of_formulas.add(formula2);
        } else {
            list_of_formulas.add(formula);
        }

        for (Map.Entry<String, Integer> entry : atom_counting_dict.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            String borrow_from = null;

            if(value == 0){
                borrow_from = getMostFrequentKey();

                if(borrow_from.equals("break_signal")){
                    break;
                }
            }

            boolean end_flag = false;


            for(ArrayList<Clause> current_formula: list_of_formulas){
                for(Clause clause : current_formula){
                    for(LogicToken token : clause){
                        if(token.tokenType.equals("ATOM") && token.value.equals("borrow_from")){
                            token.value = key;
                            end_flag = true;
                        }
                    }
                    if(end_flag == true) break;
                }
                if(end_flag == true) break;
            }

            // TODO: DOKOŃCZYĆ TO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

            //this..atom_counting_dict[key] = atom_counting_dict[key] + 1;
            //this.atom_counting_dict[borrow_from] = this.atom_counting_dict[borrow_from] - 1;

        }

        //CZ2

        for(ArrayList<Clause> current_formula: list_of_formulas){
            // TODO: DOKOŃCZYĆ !!!!
        }
    }

    private String getMostFrequentKey(){
        int highest_frequency = 0;
        String most_frequent_key = null;

        for (Map.Entry<String, Integer> entry : atom_counting_dict.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            if(value > highest_frequency){
                highest_frequency = value;
                most_frequent_key = key;
            }
        }

        if(highest_frequency == 0 || highest_frequency == 1 || most_frequent_key == null){

            if(test_type.equals("problem3")){
                return "break_signal";
            } else {
                throw new RuntimeException("Generator.getMostFrequentKey: The number of atoms is to big to use all of them.");
            }
        }

        return most_frequent_key;
    }

    // TRANSLATORY

//    private void translateToZ3(ArrayList<Clause> formula) {
//
//        // Wyciąganie zmiennych i operatorów
//
//        ArrayList<LogicToken> variableList = new ArrayList<>();
//        ArrayList<LogicToken> operatorList = new ArrayList<>();
//
//        for (Clause clause : formula) {
//            for (LogicToken token : clause.var) {
//                switch (token.tokenType) {
//
//                    case "ATOM" -> {
//                        variableList.add(token);
//                    }
//
//                    case "OR" -> {
//                        operatorList.add(token);
//                    }
//                    //
//                    case "IMP" -> {
//                        System.out.println("NIE ZDEFINIOWANE");
//                    }
//                    case "FORALL" -> {
//                        System.out.println("NIE ZDEFINIOWANE");
//                    }
//                    case "EXISTS" -> {
//                        System.out.println("NIE ZDEFINIOWANE");
//                    }
//                    default -> throw new IllegalStateException("Unexpected value: " + token.tokenType);
//                }
//            }
//        }
//
//        for (LogicToken token : variableList) {
//            // System.out.println("variableList : " + variableListObject);
//            System.out.println("(declare-fun " + token + " () Bool)");
//        }
//
//        for (LogicToken operatorListObject : operatorList) {
//            System.out.println("variableList : " + operatorListObject);
//        }
//
//
////        // Tworzenie zmiennych.
////        for(Clause clause : formula){
////            for(LogicToken token : clause.var){
////                if(token.tokenType.equals("ATOM")) {
////                    System.out.println("(declare-fun " + token + " () Bool)");
////                }
////            }
////        }
////
////        ArrayList<LogicToken> stos = new ArrayList<>();
////
////        for(Clause clause : formula){
////            StringBuilder builder = new StringBuilder();
////
////            builder.append("assert");
////
////            for(LogicToken token : clause.var){
////
////
////
////            }
//    }
}
