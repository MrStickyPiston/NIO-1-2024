import sys
import time


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

		if not self.grid[self.y][self.x] == self.get_bottom():
			temp = self.get_bottom()
			self.set_bottom(self.grid[self.y][self.x])
			self.grid[self.y][self.x] = temp

			print(f'swapped {temp} for {self.get_bottom()}')

	def _swap_sides(self, a, b, c, d, e, f):
		s = self.sides
		self.sides = [s[a], s[b], s[c], s[d], s[e], s[f]]

	def get_bottom(self):
		return self.sides[0]

	def set_bottom(self, v):
		self.sides[0] = v


def find_next(cube, direction):
	cube.rotate(direction)

	if cube.sides == [1] * 6:
		print(cube.moves)
		return

	if len(cube.moves) >= 994:
		return

	if cube.y >= 1:
		find_next(cube, 'up')
	if cube.x >= 1:
		find_next(cube, 'left')
	if cube.y <= 2:
		find_next(cube, 'down')
	if cube.x <= 2:
		find_next(cube, 'right')


def main():
	grid = [
		[0, 1, 0, 0],
		[1, 1, 0, 1],
		[0, 0, 0, 0],
		[0, 1, 0, 1]
	]

	#'up',
	#'down',
	#'right',
	#'left',

	actions = []

	cube = Cube(grid)

	for action in actions:
		cube.rotate(action)

	print(cube.sides)
	print(f'Length of {cube.moves.__len__()}')

	_grid = cube.grid
	_grid[cube.y][cube.x] = f'C{_grid[cube.y][cube.x]}'

	for i in cube.grid:
		print(*i, sep='\t')


if __name__ == '__main__':
	main()
