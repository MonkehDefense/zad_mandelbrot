// See https://aka.ms/new-console-template for more information
// Console.WriteLine("Hello, World!");
using System;
using RabbitMQ.Client;
using System.Text;

class Send
{
	public static void Main()
	{
		var host = "54.37.137.241";
		var factory = new ConnectionFactory()
		{
			HostName = host
		};

		using (var connection = factory.CreateConnection())
		using (var channel = connection.CreateModel())
		{
			channel.QueueDeclare(
				queue: "roller coaster",
				durable: false,
				exclusive: false,
				autoDelete: false,
				arguments: null);

			string message = "Skandaliczna wiadomosc.";
			var body = Encoding.UTF8.GetBytes(message);

			channel.BasicPublish(
				exchange: "",
				routingKey: "roller coaster",
				basicProperties: null,
				body: body);
				Console.WriteLine(" [x] Sent {0}", message);
		}

		Console.WriteLine(" Press [enter] to exit.");
		Console.ReadLine();
	}
}