package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

var wg sync.WaitGroup

const (
	Tobacco = "Tobacco"
	Paper   = "Paper"
	Matches = "Matches"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	table := make(chan string, 2)
	smokerDone := make(chan bool)

	wg.Add(4)


	for _, smoker := range []string{Tobacco, Paper, Matches} {
		go smokerProcess(smoker, table, smokerDone)
	}


	go dealerProcess(table, smokerDone)

	wg.Wait()
}

func smokerProcess(item string, table chan string, done chan bool) {
	for {
		fmt.Printf("%s Smoker is waiting...\n", item)
		<-table
		<-table
		fmt.Printf("%s Smoker received %s and %s, rolling and smoking...\n", item, <-table, <-table)
		time.Sleep(time.Second * time.Duration(rand.Intn(3)))
		done <- true
	}
}

func dealerProcess(table chan string, done chan bool) {
	for {
		component1, component2 := getRandomComponents()
		table <- component1
		table <- component2
		fmt.Printf("Dealer placed %s and %s on the table.\n", component1, component2)

		select {
		case <-done:

		case <-time.After(time.Second):
			fmt.Println("Dealer timed out, placing two more components on the table.")
			table <- component1
			table <- component2
		}
	}
}

func getRandomComponents() (string, string) {
	components := []string{Tobacco, Paper, Matches}
	component1 := components[rand.Intn(3)]
	component2 := component1
	for component2 == component1 {
		component2 = components[rand.Intn(3)]
	}
	return component1, component2
}
