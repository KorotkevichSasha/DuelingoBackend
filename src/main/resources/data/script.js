const { MongoClient } = require('mongodb');

async function main() {
  const client = new MongoClient('mongodb://admin:root@localhost:27017/admin?authSource=admin');
  await client.connect();

  const sourceDb = client.db('admin');
  const targetDb = client.db('admin');

  const pipeline = [
    {
      $group: {
        _id: { topic: "$topic", difficulty: "$difficulty" },
        questions: { $push: "$_id" }
      }
    },
    {
      $project: {
        _id: 0,
        topic: "$_id.topic",
        difficulty: "$_id.difficulty",
        questions: 1
      }
    }
  ];

  const results = await sourceDb.collection('questions').aggregate(pipeline).toArray();

  if (results.length > 0) {
    await targetDb.collection('tests').insertMany(results);
    console.log("Данные успешно перенесены в admin.tests!");
  } else {
    console.log("Нет данных для переноса.");
  }

  await client.close();
}

main().catch(console.error);