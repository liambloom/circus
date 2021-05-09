use std::collections::{BTreeMap, BTreeSet, HashSet};
use std::io::{BufRead, BufReader, Result as IoResult};
use std::fs::File;
use std::path::Path;
use std::hash::Hash;

const EMPLOYEES_FILE: &'static str = "../employees.txt";

fn main() -> IoResult<()> {
    let mut employees = BTreeMap::new();

    for line in BufReader::new(File::open(EMPLOYEES_FILE)?).lines().filter_map(|s| s.ok().filter(String::is_empty)) {
        Employee::add_from_string(&mut employees, line);
    }



    Ok(())
}

struct Employee<'a> {
    firstname: String,
    lastname: String,
    middle_initial: char,
    id_num: String,
    category: &'a str,
    title: String,
}

impl<'a> Employee<'a> {
    fn add_from_string(map: &mut BTreeMap<String, BTreeSet<Employee>>, s: String) -> Employee<'a> {
        todo!();
    }
}

struct Interner<'a, T: Eq + Hash>(HashSet<&'a T>);

impl<'a, T: Eq + Hash> Interner<'a, T> {
    pub fn new() -> Self {
        Self(HashSet::new())
    }

    pub fn intern(&mut self, e: &'a T) -> &'a T {
        // Unfortunately, get_or_insert is nightly-only
        match self.0.get(e) {
            Some(r) => r,
            None => {
                self.0.insert(e);
                e
            }
        }
    }
}
