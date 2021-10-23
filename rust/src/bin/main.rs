use std::collections::BTreeMap;
use std::io::{BufRead, BufReader, Result as IoResult};
use std::fs::File;

use circus::Employee;

const EMPLOYEES_FILE: &'static str = "../employees.txt";

fn main() -> IoResult<()> {
    let mut employees = BTreeMap::new();

    for line in BufReader::new(File::open(EMPLOYEES_FILE)?).lines().filter_map(|s| s.ok().filter(String::is_empty)) {
        Employee::add_from_string(&mut employees, line);
    }



    Ok(())
}


