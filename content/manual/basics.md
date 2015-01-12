---
date: 2015-01-02T22:06:44+01:00
title: "Basics"
weight: 30
menu:
  main:
    parent: manual
    identifier: basics
---

# Basics


- Quick start
- Getting started
 - Introduction
 - Installation
 - 
- Installation




### Safe

Our query asks for entities having values defined for all three attributes. If some entity doesn't have the `street` attribute set it won't be returned. So we can safely assume that our result set contains no null values and we therefore return the raw values (without using Optional for instance).