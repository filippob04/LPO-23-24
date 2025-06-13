#!/bin/bash

## Per Eseguire e Compilare: bash script.sh -c
## Per Eseguire: bash script.sh

## Checking arguments...
compile=false
if [[ "$1" == "-c" || "$1" == "--compile" ]]; then
    compile=true
    shift
fi

##################################################

bold=$(tput bold)
green=$(tput setaf 2)
reset=$(tput sgr0)

##################################################

## Compiling
if $compile; then
    echo
    echo "${bold}Compiling java project...${reset}"
    find ./projectLabo -name "*.java" -exec javac -d ./bin {} \;
    echo
    echo "${bold}Compiling completed, starting test:${reset}"
fi

##################################################

## Declaring start, success, failure, and extraTests command
start="java -cp bin projectLabo.Main"

successTestDir="./tests/success"
failureTestDir="./tests/failure/"
customTestDir="./tests/custom/"

path="."
fileNo=$(ls -1 "$path" | wc -l)

##################################################

## TESTS ##

## Failure tests
echo
echo "${bold}Starting Failure tests...${reset}"

    ### Syntax error tests ###
    echo
    echo "${bold}Starting Syntax error tests...${reset}"

    fileNo=$(ls -1 "./tests/failure/syntax" | wc -l) 
    
    for ((i=1; i<=fileNo; i++))
    do
        echo
        echo "${green}Test n.$i${reset}"
        inputFile="$failureTestDir/syntax/prog0$i.txt"
        $start -i "$inputFile"
    done


    ### static-semantics-only tests ###
    echo
    echo "${bold}Starting static-semantics-only tests...${reset}"

    fileNo=$(ls -1 "./tests/failure/static-semantics-only" | wc -l) 

    for ((i=1; i<=fileNo; i++))
    do
        echo
        echo "${green}With -ntc enabled - Test n.$i${reset}"
        $start -ntc -i "$failureTestDir/static-semantics-only/prog0$i.txt"
        
        echo
        echo "${green}Without -ntc enabled - Test n.$i${reset}"
        $start -i "$failureTestDir/static-semantics-only/prog0$i.txt"
    done
    


    ### Static-semantics tests ### 
    echo
    echo "${bold}Starting Static-Semantics tests...${reset}"

    fileNo=$(ls -1 "./tests/failure/static-semantics" | wc -l) 

    for ((i=1; i<=fileNo; i++))
    do
        echo
        echo "${green}Test n.$i${reset}"
        inputFile="$failureTestDir/static-semantics/prog0$i.txt"
        $start -i "$inputFile"
    done


    ### Dynamic-semantics tests ###
    echo
    echo "${bold}Starting Dynamic-Semantics-Only tests..."

    fileNo=$(ls -1 "./tests/failure/dynamic-semantics-only" | wc -l) 

    for ((i=1; i<=fileNo; i++))
    do
        echo
        echo "${green}Test n.$i${reset}"
        inputFile="$failureTestDir/dynamic-semantics-only/prog0$i.txt"
        $start -i "$inputFile"
    done

## Success tests
echo
echo "${bold}Starting Success tests...${reset}"

    fileNo=$(ls -1 "./tests/success" | wc -l) 

    for ((i=1; i<=fileNo; i++))
    do
        echo
        echo "${green}Test n.$i${reset}"
        inputFile="$successTestDir/prog0$i.txt"
        $start -i "$inputFile"
    done

## Custom tests
echo
echo "${bold}Starting Custom tests...${reset}"

    fileNo=$(ls -1 "./tests/custom" | wc -l) 

    echo
    echo "${bold}Without -ntc enabled...${reset}"
    for ((i=1; i<=fileNo; i++))
    do
            
        echo
        echo "${green}Without -ntc enabled - Test n.$i${reset}"
        $start -i "$customTestDir/prog0$i.txt"
    done

    echo
    echo "${bold}With -ntc enabled...${reset}"
    for ((i=1; i<=fileNo; i++))
    do
        echo
        echo "${green}With -ntc enabled - Test n.$i${reset}"
        $start -ntc -i "$customTestDir/prog0$i.txt"
    done
    echo
