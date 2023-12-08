import itertools
from copy import deepcopy


class Cube:
	def __init__(self, grid):
		self.sides = [0, 0, 0, 0, 0, 0]
		self.grid = grid
		self.x = 0
		self.y = 0

		self.moves = []

	def rotate(self, direction):
		if direction == 'up':
			self._swap_sides(4, 1, 0, 3, 5, 2)
			self.y -= 1
		elif direction == 'down':
			self._swap_sides(2, 1, 5, 3, 0, 4)
			self.y += 1
		elif direction == 'left':
			self._swap_sides(1, 5, 2, 0, 4, 3)
			self.x -= 1
		elif direction == 'right':
			self._swap_sides(3, 0, 2, 5, 4, 1)
			self.x += 1
		else:
			print(f"Invalid direction: {direction}")
			return

		self.moves.append(direction)

		if not -1 < self.x < 4 or not -1 < self.y < 4:
			raise ValueError()

		if not self.grid[self.y][self.x] == self.get_bottom():
			temp = self.get_bottom()
			self.set_bottom(self.grid[self.y][self.x])
			self.grid[self.y][self.x] = temp

			#print(f'swapped {temp} for {self.get_bottom()}')

	def _swap_sides(self, a, b, c, d, e, f):
		s = self.sides
		self.sides = [s[a], s[b], s[c], s[d], s[e], s[f]]

	def get_bottom(self):
		return self.sides[0]

	def set_bottom(self, v):
		self.sides[0] = v


def main():
	grid = [
		[0, 1, 0, 0],
		[1, 1, 0, 1],
		[0, 0, 0, 0],
		[0, 1, 0, 1]
	]

	actions = [
		'up',
		'down',
		'right',
		'left'
	]

	max_actions = 20

	result = []

	for i in range(10, max_actions + 1):
		print(f'checking {i}')
		routes = itertools.product(actions, repeat=i)

		for route in routes:
			cube = Cube(deepcopy(grid))

			try:
				for action in route:
					cube.rotate(action)
			except ValueError:
				continue

			if cube.sides == [1]*6:
				result.append(route)
				print(cube.moves)

	print(result)


if __name__ == '__main__':
	main()
