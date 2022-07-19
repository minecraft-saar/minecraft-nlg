<template>
  <q-card flat id="mccanvas"/>
</template>

<script setup>
// most code is from: https://r105.threejsfundamentals.org/threejs/lessons/threejs-voxel-geometry.html

import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { defineProps, computed, onMounted, watch } from 'vue';
import textureBlocks from '../resources/textures.png';

function getColorCode(sColor) {
    if (sColor == "stone") {
        return 1;
    } else if (sColor == "blue_wool") {
        return 3;
    } else if (sColor == "red_wool") {
        return 2;
    }

    return 1;
}

function minValues(blocksList) {
    let minX = Number.MAX_SAFE_INTEGER;
    let minY = Number.MAX_SAFE_INTEGER;
    let minZ = Number.MAX_SAFE_INTEGER;

    for (let block of blocksList) {
        if (block.x < minX) {
            minX = block.x;
        }
        if (block.y < minY) {
            minY = block.y;
        }
        if (block.z < minZ) {
            minZ = block.z;
        }
    }

    return {minX, minY, minZ};
}

function maxValues(blocksList) {
    let maxX = 0;
    let maxY = 0;
    let maxZ = 0;

    for (let block of blocksList) {
        if (block.x > maxX) {
            maxX = block.x;
        }
        if (block.y > maxY) {
            maxY = block.y;
        }
        if (block.z > maxZ) {
            maxZ = block.z;
        }
    }

    return {maxX, maxY, maxZ};
}

function normalize(blocksList) {
    const {minX, minY, minZ} = minValues(blocksList);

    let newBlocks = [];
    for (let block of blocksList) {
        newBlocks.push({x: block.x-minX, y: block.y-minY+1, z: block.z-minZ, color: block.color});
    }
    return newBlocks;
}

const props = defineProps(["blockList"]);
const blockList = computed(() => normalize(props.blockList));

class VoxelWorld {
  constructor(options) {
    this.cellSize = options.cellSize;
    this.tileSize = options.tileSize;
    this.tileTextureWidth = options.tileTextureWidth;
    this.tileTextureHeight = options.tileTextureHeight;
    const {cellSize} = this;
    this.cellSliceSize = cellSize * cellSize;
    this.cell = new Uint8Array(cellSize * cellSize * cellSize);
  }
  computeVoxelOffset(x, y, z) {
    const {cellSize, cellSliceSize} = this;
    const voxelX = THREE.MathUtils.euclideanModulo(x, cellSize) | 0;
    const voxelY = THREE.MathUtils.euclideanModulo(y, cellSize) | 0;
    const voxelZ = THREE.MathUtils.euclideanModulo(z, cellSize) | 0;
    return voxelY * cellSliceSize +
           voxelZ * cellSize +
           voxelX;
  }
  getCellForVoxel(x, y, z) {
    const {cellSize} = this;
    const cellX = Math.floor(x / cellSize);
    const cellY = Math.floor(y / cellSize);
    const cellZ = Math.floor(z / cellSize);
    if (cellX !== 0 || cellY !== 0 || cellZ !== 0) {
      return null;
    }
    return this.cell;
  }
  setVoxel(x, y, z, v) {
    const cell = this.getCellForVoxel(x, y, z);
    if (!cell) {
      return;  // TODO: add a new cell?
    }
    const voxelOffset = this.computeVoxelOffset(x, y, z);
    cell[voxelOffset] = v;
  }
  getVoxel(x, y, z) {
    const cell = this.getCellForVoxel(x, y, z);
    if (!cell) {
      return 0;
    }
    const voxelOffset = this.computeVoxelOffset(x, y, z);
    return cell[voxelOffset];
  }
  generateGeometryDataForCell(cellX, cellY, cellZ) {
    const {cellSize, tileSize, tileTextureWidth, tileTextureHeight} = this;
    const positions = [];
    const normals = [];
    const uvs = [];
    const indices = [];
    const startX = cellX * cellSize;
    const startY = cellY * cellSize;
    const startZ = cellZ * cellSize;

    for (let y = 0; y < cellSize; ++y) {
      const voxelY = startY + y;
      for (let z = 0; z < cellSize; ++z) {
        const voxelZ = startZ + z;
        for (let x = 0; x < cellSize; ++x) {
          const voxelX = startX + x;
          const voxel = this.getVoxel(voxelX, voxelY, voxelZ);
          if (voxel) {
            // voxel 0 is sky (empty) so for UVs we start at 0
            const uvVoxel = voxel - 1;
            // There is a voxel here but do we need faces for it?
            for (const {dir, corners, uvRow} of VoxelWorld.faces) {
              const neighbor = this.getVoxel(
                  voxelX + dir[0],
                  voxelY + dir[1],
                  voxelZ + dir[2]);
              if (!neighbor) {
                // this voxel has no neighbor in this direction so we need a face.
                const ndx = positions.length / 3;
                for (const {pos, uv} of corners) {
                  positions.push(pos[0] + x, pos[1] + y, pos[2] + z);
                  normals.push(...dir);
                  uvs.push(
                        (uvVoxel +     uv[0]) * tileSize / tileTextureWidth,
                    1 - (uvRow + 1 - uv[1]) * tileSize / tileTextureHeight);
                }
                indices.push(
                  ndx, ndx + 1, ndx + 2,
                  ndx + 2, ndx + 1, ndx + 3,
                );
              }
            }
          }
        }
      }
    }

    return {
      positions,
      normals,
      uvs,
      indices,
    };
  }
}

VoxelWorld.faces = [
  { // left
    uvRow: 0,
    dir: [ -1,  0,  0, ],
    corners: [
      { pos: [ 0, 1, 0 ], uv: [ 0, 1 ], },
      { pos: [ 0, 0, 0 ], uv: [ 0, 0 ], },
      { pos: [ 0, 1, 1 ], uv: [ 1, 1 ], },
      { pos: [ 0, 0, 1 ], uv: [ 1, 0 ], },
    ],
  },
  { // right
    uvRow: 0,
    dir: [  1,  0,  0, ],
    corners: [
      { pos: [ 1, 1, 1 ], uv: [ 0, 1 ], },
      { pos: [ 1, 0, 1 ], uv: [ 0, 0 ], },
      { pos: [ 1, 1, 0 ], uv: [ 1, 1 ], },
      { pos: [ 1, 0, 0 ], uv: [ 1, 0 ], },
    ],
  },
  { // bottom
    uvRow: 1,
    dir: [  0, -1,  0, ],
    corners: [
      { pos: [ 1, 0, 1 ], uv: [ 1, 0 ], },
      { pos: [ 0, 0, 1 ], uv: [ 0, 0 ], },
      { pos: [ 1, 0, 0 ], uv: [ 1, 1 ], },
      { pos: [ 0, 0, 0 ], uv: [ 0, 1 ], },
    ],
  },
  { // top
    uvRow: 2,
    dir: [  0,  1,  0, ],
    corners: [
      { pos: [ 0, 1, 1 ], uv: [ 1, 1 ], },
      { pos: [ 1, 1, 1 ], uv: [ 0, 1 ], },
      { pos: [ 0, 1, 0 ], uv: [ 1, 0 ], },
      { pos: [ 1, 1, 0 ], uv: [ 0, 0 ], },
    ],
  },
  { // back
    uvRow: 0,
    dir: [  0,  0, -1, ],
    corners: [
      { pos: [ 1, 0, 0 ], uv: [ 0, 0 ], },
      { pos: [ 0, 0, 0 ], uv: [ 1, 0 ], },
      { pos: [ 1, 1, 0 ], uv: [ 0, 1 ], },
      { pos: [ 0, 1, 0 ], uv: [ 1, 1 ], },
    ],
  },
  { // front
    uvRow: 0,
    dir: [  0,  0,  1, ],
    corners: [
      { pos: [ 0, 0, 1 ], uv: [ 0, 0 ], },
      { pos: [ 1, 0, 1 ], uv: [ 1, 0 ], },
      { pos: [ 0, 1, 1 ], uv: [ 0, 1 ], },
      { pos: [ 1, 1, 1 ], uv: [ 1, 1 ], },
    ],
  },
];

let removed = false;

const webglf = () => {
    const canvasContainer = document.getElementById("mccanvas");
    while (canvasContainer.firstChild) {
        canvasContainer.removeChild(canvasContainer.lastChild);
    }
    
    const renderer = new THREE.WebGLRenderer({antialias: true});
    renderer.setSize(600, 300);
    canvasContainer.appendChild(renderer.domElement);

    if (!removed) {
      var tooltip = document.createElement('div');
      tooltip.className = 'absolute-bottom-right hiddenchild';
      tooltip.style = 'border-top-left-radius: 5px;background:rgba(0, 0, 0, 0.55);color:white;padding:2px;padding-left:5px;';
      tooltip.innerHTML = 'drag to move the camera, scroll to zoom';
      canvasContainer.appendChild(tooltip);
      
      var tooltipX = document.createElement('i');
      tooltipX.className = 'absolute-top-right text-body1 q-icon material-symbols-outlined notranslate material-icons';
      tooltipX.style = 'border-top-left-radius: 5px;color:rgba(255, 255, 255, 0.85);padding:2px;padding-left:5px;transform: translate(2px, -9px)';
      tooltipX.innerHTML = 'highlight_off';
      tooltip.appendChild(tooltipX);
      tooltipX.addEventListener("click", function() {
        canvasContainer.removeChild(tooltip);
        removed = true;
      });
    }

    const scene = new THREE.Scene();
    scene.background = new THREE.Color('lightblue');

    const fov = 50;
    const aspect = .2;  
    const near = 0.1;
    const far = 1000;
    const camera = new THREE.PerspectiveCamera(fov, aspect, near, far);

    const {maxX, maxY, maxZ} = maxValues(blockList.value);
    const cellSize = Math.max(maxX, Math.max(maxY, maxZ))+1;
    const canvas = renderer.domElement;
    const controls = new OrbitControls(camera, canvas);
    controls.target.set(cellSize / 2, cellSize / 3, cellSize / 2);
    controls.update();

    function addLight(x, y, z) {
        const color = 0xFFFFFF;
        const intensity = 1;
        const light = new THREE.DirectionalLight(color, intensity);
        light.position.set(x, y, z);
        scene.add(light);
    }
    addLight(-1,  2,  4);
    addLight( 1, -1, -2);

    const loader = new THREE.TextureLoader();
    const texture = loader.load(textureBlocks, render);
    texture.magFilter = THREE.NearestFilter;
    texture.minFilter = THREE.NearestFilter;

    const tileSize = 16;
    const tileTextureWidth = 256;
    const tileTextureHeight = 64;
    const world = new VoxelWorld({
        cellSize,
        tileSize,
        tileTextureWidth,
        tileTextureHeight,
    });

    //blocks
    for (let block of blockList.value) {
        world.setVoxel(block.x, block.y, block.z, getColorCode(block.color));
    }
    //floor
    for (let x = 0; x < cellSize; x++) {
        for (let z = 0; z < cellSize; z++) {
            world.setVoxel(x, 0, z, 14);
        }
    }

    const {positions, normals, uvs, indices} = world.generateGeometryDataForCell(0, 0, 0);
    const geometry = new THREE.BufferGeometry();
    const material = new THREE.MeshLambertMaterial({
        map: texture,
        side: THREE.DoubleSide,
        alphaTest: 0.1,
        transparent: true,
    });


    const positionNumComponents = 3;
    const normalNumComponents = 3;
    const uvNumComponents = 2;
    geometry.setAttribute(
      'position',
      new THREE.BufferAttribute(new Float32Array(positions), positionNumComponents));
    geometry.setAttribute(
        'normal',
        new THREE.BufferAttribute(new Float32Array(normals), normalNumComponents));
    geometry.setAttribute(
        'uv',
        new THREE.BufferAttribute(new Float32Array(uvs), uvNumComponents));
    geometry.setIndex(indices);
    const mesh = new THREE.Mesh(geometry, material);
    scene.add(mesh);
    
    camera.aspect = canvas.clientWidth / canvas.clientHeight;
    camera.updateProjectionMatrix();

    let renderRequested = false;

    function resizeRendererToDisplaySize(renderer) {
        const canvas = renderer.domElement;
        const width = canvas.clientWidth;
        const height = canvas.clientHeight;
        const needResize = canvas.width !== width || canvas.height !== height;
        if (needResize) {
            renderer.setSize(width, height, false);
        }
        return needResize;
    }

    function render() {
        renderRequested = undefined;

        if (resizeRendererToDisplaySize(renderer)) {
            const canvas = renderer.domElement;
            camera.aspect = canvas.clientWidth / canvas.clientHeight;
            camera.updateProjectionMatrix();
        }

        controls.update();
        renderer.render(scene, camera);
    }
    render();

    function requestRenderIfNotRequested() {
        if (!renderRequested) {
            renderRequested = true;
            requestAnimationFrame(render);
        }
    }

    controls.addEventListener('change', requestRenderIfNotRequested);
}

onMounted(webglf);
watch(blockList, () => {
    webglf();
});

</script>

<style lang="css">
.hiddenchild > i {
    visibility: hidden;
}
.hiddenchild:hover > i {
    visibility: visible;
}
.hiddenchild > i:hover {
    visibility: visible;
}
</style>