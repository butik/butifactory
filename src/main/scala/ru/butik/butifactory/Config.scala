package ru.butik.butifactory

case class Config(db: String,
                  addr: String,
                  dataDir: String,
                  servePath: String,
                  pushHost: String,
                  pushPort: String,
                  pushMethod: String,
                  pushKey: String)